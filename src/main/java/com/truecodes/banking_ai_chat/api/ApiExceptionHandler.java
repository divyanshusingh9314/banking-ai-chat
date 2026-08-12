package com.truecodes.banking_ai_chat.api;

import com.truecodes.banking_ai_chat.exception.AiProviderException;
import com.truecodes.banking_ai_chat.exception.AiProviderRetryableException;
import com.truecodes.banking_ai_chat.exception.AiResponseFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    ResponseEntity<ApiError> validation(WebExchangeBindException exception) {
        return ResponseEntity.badRequest().body(new ApiError(
                "INVALID_REQUEST",
                "Request validation failed",
                null
        ));
    }

    @ExceptionHandler(AiProviderRetryableException.class)
    ResponseEntity<ApiError> providerUnavailable(AiProviderRetryableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiError(
                "AI_PROVIDER_UNAVAILABLE",
                "AI service is temporarily unavailable. Please try again.",
                exception.providerRequestId()
        ));
    }

    @ExceptionHandler(AiProviderException.class)
    ResponseEntity<ApiError> providerError(AiProviderException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiError(
                "AI_PROVIDER_ERROR",
                "AI provider request failed.",
                exception.providerRequestId()
        ));
    }

    @ExceptionHandler(AiResponseFormatException.class)
    ResponseEntity<ApiError> invalidAiResponse(AiResponseFormatException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiError(
                "INVALID_AI_RESPONSE",
                "AI response could not be processed.",
                null
        ));
    }

    public record ApiError(
            String code,
            String message,
            String providerRequestId
    ) {
    }
}