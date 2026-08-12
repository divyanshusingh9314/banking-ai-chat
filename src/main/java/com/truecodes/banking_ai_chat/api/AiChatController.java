package com.truecodes.banking_ai_chat.api;


import com.truecodes.banking_ai_chat.service.BankChatService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ai")
public class AiChatController {

    private final BankChatService bankChatService;

    public AiChatController(BankChatService bankChatService) {
        this.bankChatService = bankChatService;
    }

    @PostMapping("/chat")
    public Mono<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId
    ) {
        String traceId = StringUtils.hasText(correlationId)
                ? correlationId
                : UUID.randomUUID().toString();

        return bankChatService.chat(request, traceId);
    }
}