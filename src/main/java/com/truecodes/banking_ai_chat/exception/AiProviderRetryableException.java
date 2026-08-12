package com.truecodes.banking_ai_chat.exception;

public class AiProviderRetryableException extends AiProviderException {
    public AiProviderRetryableException(int statusCode, String message, String providerRequestId) {
        super(statusCode, message, providerRequestId);
    }
}