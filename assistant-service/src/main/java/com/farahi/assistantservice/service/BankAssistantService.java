package com.farahi.assistantservice.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class BankAssistantService {

    private final ChatClient chatClient;

    public BankAssistantService(ChatModel chatModel, SyncMcpToolCallbackProvider mcpTools) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("Tu es un assistant bancaire de démonstration. Utilise les outils MCP pour consulter ou modifier les comptes et les transactions. Ne prétends jamais avoir effectué une opération sans réponse positive de l'outil. Réponds en français, brièvement, et demande les informations manquantes avant une opération.")
                .defaultTools(mcpTools)
                .build();
    }

    public String respond(String message) {
        return chatClient.prompt().user(message).call().content();
    }
}
