package com.truecodes.banking_ai_chat.service;

import com.truecodes.banking_ai_chat.api.ChatRequest;
import com.truecodes.banking_ai_chat.api.ChatResponse;
import com.truecodes.banking_ai_chat.config.OpenAiProperties;
import com.truecodes.banking_ai_chat.exception.AiResponseFormatException;
import com.truecodes.banking_ai_chat.openai.OpenAiClient;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.InputContent;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.InputMessage;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.OpenAiCallResult;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.OutputContent;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.OutputItem;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.ResponsesRequest;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.ResponsesResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
public class BankChatService {

    private static final String BANKING_INSTRUCTIONS = """
            You are a banking app customer support assistant.

            Safety and compliance rules:
            - Do not ask for or reveal full account numbers, SSNs, PINs, CVV codes, full card numbers, or one-time passcodes.
            - Do not claim that you completed a transfer, dispute, password reset, card lock, loan action, or profile update.
            - For money movement, account-specific balances, suspected fraud, disputes, identity changes, or legal and credit advice, explain the secure next step.
            - Keep answers short, practical, and suitable for a mobile banking app.
            - Output only JSON that matches the requested schema.
            """;

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public BankChatService(
            OpenAiClient openAiClient,
            OpenAiProperties properties,
            ObjectMapper objectMapper
    ) {
        this.openAiClient = openAiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Mono<ChatResponse> chat(ChatRequest request, String correlationId) {
        ResponsesRequest providerRequest = new ResponsesRequest(
                properties.model(),
                BANKING_INSTRUCTIONS,
                List.of(new InputMessage(
                        "user",
                        List.of(new InputContent("input_text", buildUserPrompt(request)))
                )),
                structuredTextFormat(),
                Map.of("effort", "low"),
                properties.maxOutputTokens(),
                false,
                Map.of("feature", "banking_chatbot")
        );

        return openAiClient.createResponse(providerRequest, correlationId)
                .map(result -> toChatResponse(result, request.sessionId()));
    }

    private String buildUserPrompt(ChatRequest request) {
        String locale = StringUtils.hasText(request.locale()) ? request.locale() : "en-US";

        return """
                User locale: %s

                User message:
                %s
                """.formatted(locale, request.message());
    }

    private Map<String, Object> structuredTextFormat() {
        return Map.of(
                "format", Map.of(
                        "type", "json_schema",
                        "name", "bank_chatbot_response",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "reply", Map.of("type", "string"),
                                        "category", Map.of(
                                                "type", "string",
                                                "enum", List.of(
                                                        "general_support",
                                                        "card_dispute",
                                                        "account_access",
                                                        "payments_transfers",
                                                        "fraud_security",
                                                        "loan_credit",
                                                        "other"
                                                )
                                        ),
                                        "requiresHumanReview", Map.of("type", "boolean"),
                                        "riskLevel", Map.of(
                                                "type", "string",
                                                "enum", List.of("low", "medium", "high")
                                        )
                                ),
                                "required", List.of(
                                        "reply",
                                        "category",
                                        "requiresHumanReview",
                                        "riskLevel"
                                )
                        )
                )
        );
    }

    private ChatResponse toChatResponse(OpenAiCallResult result, String sessionId) {
        ResponsesResponse response = result.response();

        if (response.error() != null) {
            throw new AiResponseFormatException(
                    "AI response failed: " + response.error().message()
            );
        }

        if (!"completed".equals(response.status())) {
            String reason = response.incompleteDetails() == null
                    ? response.status()
                    : response.incompleteDetails().reason();

            throw new AiResponseFormatException("AI response did not complete: " + reason);
        }

        String outputText = extractOutputText(response);

        try {
            BankingAiPayload payload = objectMapper.readValue(outputText, BankingAiPayload.class);

            return new ChatResponse(
                    payload.reply(),
                    payload.category(),
                    payload.requiresHumanReview(),
                    payload.riskLevel(),
                    sessionId,
                    response.id(),
                    result.providerRequestId()
            );
        } catch (JsonProcessingException exception) {
            throw new AiResponseFormatException("AI returned invalid JSON", exception);
        }
    }

    private String extractOutputText(ResponsesResponse response) {
        StringJoiner text = new StringJoiner("\n");
        List<String> refusals = new ArrayList<>();

        if (response.output() != null) {
            for (OutputItem item : response.output()) {
                if (item.content() == null) {
                    continue;
                }

                for (OutputContent content : item.content()) {
                    if ("refusal".equals(content.type()) && StringUtils.hasText(content.refusal())) {
                        refusals.add(content.refusal());
                    }

                    if ("output_text".equals(content.type()) && StringUtils.hasText(content.text())) {
                        text.add(content.text());
                    }
                }
            }
        }

        if (!refusals.isEmpty()) {
            throw new AiResponseFormatException("AI refused the request: " + refusals.get(0));
        }

        String outputText = text.toString();

        if (!StringUtils.hasText(outputText)) {
            throw new AiResponseFormatException("AI response did not contain output_text");
        }

        return outputText;
    }

    private record BankingAiPayload(
            String reply,
            String category,
            boolean requiresHumanReview,
            String riskLevel
    ) {
    }
}