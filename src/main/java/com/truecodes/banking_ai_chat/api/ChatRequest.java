package com.truecodes.banking_ai_chat.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]{3,80}$")
        String sessionId,

        @NotBlank
        @Size(max = 2000)
        String message,

        @Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$")
        String locale
) {
}