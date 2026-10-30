package com.farahi.assistantservice.telegram;

import com.farahi.assistantservice.service.BankAssistantService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "app.telegram", name = "enabled", havingValue = "true")
public class TelegramBot {

    private final BankAssistantService assistantService;
    private final RestClient telegram;
    private final long allowedChatId;
    private long offset;

    public TelegramBot(BankAssistantService assistantService,
                       @Value("${app.telegram.token}") String token,
                       @Value("${app.telegram.allowed-chat-id}") String allowedChatId) {
        if (token.isBlank() || allowedChatId.isBlank()) {
            throw new IllegalArgumentException("Telegram requiert TELEGRAM_BOT_TOKEN et TELEGRAM_ALLOWED_CHAT_ID");
        }
        this.assistantService = assistantService;
        this.telegram = RestClient.builder().baseUrl("https://api.telegram.org/bot" + token).build();
        this.allowedChatId = Long.parseLong(allowedChatId);
    }

    @Scheduled(fixedDelay = 5000)
    @SuppressWarnings("unchecked")
    public void poll() {
        Map<String, Object> response;
        try {
            response = telegram.post().uri("/getUpdates")
                    .body(Map.of("offset", offset, "timeout", 10, "allowed_updates", List.of("message")))
                    .retrieve().body(Map.class);
        } catch (Exception ignored) {
            return;
        }
        if (response == null || !Boolean.TRUE.equals(response.get("ok"))) {
            return;
        }
        for (Map<String, Object> update : (List<Map<String, Object>>) response.getOrDefault("result", List.of())) {
            offset = ((Number) update.get("update_id")).longValue() + 1;
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            if (message == null) {
                continue;
            }
            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            if (chat == null || ((Number) chat.get("id")).longValue() != allowedChatId) {
                continue;
            }
            String text = (String) message.get("text");
            if (text == null || text.isBlank()) {
                continue;
            }
            String answer;
            try {
                answer = assistantService.respond(text);
            } catch (Exception ignored) {
                answer = "L'opération n'a pas pu être vérifiée. Réessaie plus tard.";
            }
            try {
                telegram.post().uri("/sendMessage")
                        .body(Map.of("chat_id", allowedChatId, "text", answer))
                        .retrieve().toBodilessEntity();
            } catch (Exception ignored) {
                // Une panne Telegram ne doit pas interrompre le serveur REST.
            }
        }
    }
}
