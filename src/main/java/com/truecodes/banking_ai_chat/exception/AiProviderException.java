package com.truecodes.banking_ai_chat.exception;

public class AiProviderException extends RuntimeException {
    private final int statusCode;
    private final String providerRequestId;

    public AiProviderException(int statusCode, String message, String providerRequestId) {
        super(message);
        this.statusCode = statusCode;
        this.providerRequestId = providerRequestId;
    }

    public int statusCode() {
        return statusCode;
    }

    public String providerRequestId() {
        return providerRequestId;
    }
}