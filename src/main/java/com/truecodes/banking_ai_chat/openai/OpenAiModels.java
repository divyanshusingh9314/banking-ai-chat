package com.truecodes.banking_ai_chat.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public final class OpenAiModels {
    private OpenAiModels() {
    }

    public record ResponsesRequest(
            String model,
            String instructions,
            List<InputMessage> input,
            Map<String, Object> text,
            Map<String, String> reasoning,
            @JsonProperty("max_output_tokens") int maxOutputTokens,
            boolean store,
            Map<String, String> metadata
    ) {
    }

    public record InputMessage(String role, List<InputContent> content) {
    }

    public record InputContent(String type, String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponsesResponse(
            String id,
            String status,
            ProviderError error,
            @JsonProperty("incomplete_details") IncompleteDetails incompleteDetails,
            List<OutputItem> output,
            Usage usage
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OutputItem(String type, String role, List<OutputContent> content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OutputContent(String type, String text, String refusal) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IncompleteDetails(String reason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
            @JsonProperty("input_tokens") Integer inputTokens,
            @JsonProperty("output_tokens") Integer outputTokens,
            @JsonProperty("total_tokens") Integer totalTokens
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProviderError(String message, String type, String param, String code) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorResponse(ProviderError error) {
    }

    public record OpenAiCallResult(
            ResponsesResponse response,
            String providerRequestId
    ) {
    }
}