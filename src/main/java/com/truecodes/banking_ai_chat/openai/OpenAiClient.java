package com.truecodes.banking_ai_chat.openai;

import com.truecodes.banking_ai_chat.config.OpenAiProperties;
import com.truecodes.banking_ai_chat.exception.AiProviderException;
import com.truecodes.banking_ai_chat.exception.AiProviderRetryableException;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.ErrorResponse;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.OpenAiCallResult;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.ProviderError;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.ResponsesRequest;
import com.truecodes.banking_ai_chat.openai.OpenAiModels.ResponsesResponse;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final WebClient openAiWebClient;
    private final OpenAiProperties properties;

    public OpenAiClient(WebClient openAiWebClient, OpenAiProperties properties) {
        this.openAiWebClient = openAiWebClient;
        this.properties = properties;
    }

    public Mono<OpenAiCallResult> createResponse(ResponsesRequest request, String correlationId) {
        String clientRequestId = UUID.randomUUID().toString();

        return openAiWebClient.post()
                .uri("/responses")
                .header("X-Client-Request-Id", clientRequestId)
                .bodyValue(request)
                .exchangeToMono(this::handleResponse)
                .timeout(properties.timeout())
                .retryWhen(Retry.backoff(
                                properties.retry().maxRetries(),
                                properties.retry().minBackoff()
                        )
                        .maxBackoff(properties.retry().maxBackoff())
                        .jitter(0.4)
                        .filter(this::isRetryable)
                        .doBeforeRetry(signal -> log.warn(
                                "Retrying AI provider call. correlationId={}, retry={}, reason={}",
                                correlationId,
                                signal.totalRetries() + 1,
                                signal.failure().toString()
                        ))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    private Mono<OpenAiCallResult> handleResponse(ClientResponse response) {
        String providerRequestId = response.headers()
                .asHttpHeaders()
                .getFirst("x-request-id");

        if (response.statusCode().isError()) {
            return response.bodyToMono(ErrorResponse.class)
                    .onErrorReturn(new ErrorResponse(
                            new ProviderError("Unable to parse provider error response", null, null, null)
                    ))
                    .defaultIfEmpty(new ErrorResponse(
                            new ProviderError("Empty provider error response", null, null, null)
                    ))
                    .flatMap(error -> Mono.error(toException(
                            response.statusCode(),
                            error,
                            providerRequestId
                    )));
        }

        return response.bodyToMono(ResponsesResponse.class)
                .map(body -> new OpenAiCallResult(body, providerRequestId));
    }

    private RuntimeException toException(
            HttpStatusCode status,
            ErrorResponse errorResponse,
            String providerRequestId
    ) {
        String message = errorResponse.error() == null
                ? "AI provider request failed"
                : errorResponse.error().message();

        if (status.value() == 408 || status.value() == 429 || status.is5xxServerError()) {
            return new AiProviderRetryableException(status.value(), message, providerRequestId);
        }

        return new AiProviderException(status.value(), message, providerRequestId);
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof AiProviderRetryableException
                || throwable instanceof WebClientRequestException
                || throwable instanceof TimeoutException;
    }
}