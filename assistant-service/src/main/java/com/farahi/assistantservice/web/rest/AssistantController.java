package com.farahi.assistantservice.web.rest;

import com.farahi.assistantservice.service.BankAssistantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant")
public class AssistantController {

    private final BankAssistantService assistantService;

    public AssistantController(BankAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(new ChatResponse(assistantService.respond(request.message())));
    }

    public record ChatRequest(String message) {}

    public record ChatResponse(String answer) {}
}
