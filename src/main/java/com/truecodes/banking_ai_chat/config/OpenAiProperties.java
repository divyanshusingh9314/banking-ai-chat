package com.truecodes.banking_ai_chat.config;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ai.openai")
public record OpenAiProperties(
        @NotBlank String baseUrl,
        @NotBlank String apiKey,
        @NotBlank String model,
        @NotNull Duration timeout,
        @Min(1) @Max(4096) int maxOutputTokens,
        @NotNull @Valid Retry retry
) {
    public record Retry(
            @Min(1) @Max(5) long maxRetries,
            @NotNull Duration minBackoff,
            @NotNull Duration maxBackoff
    ) {
    }
}