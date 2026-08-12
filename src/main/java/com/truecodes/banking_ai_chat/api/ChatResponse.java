package com.truecodes.banking_ai_chat.api;

public record ChatResponse(
        String reply,
        String category,
        boolean requiresHumanReview,
        String riskLevel,
        String sessionId,
        String providerResponseId,
        String providerRequestId
) {
}