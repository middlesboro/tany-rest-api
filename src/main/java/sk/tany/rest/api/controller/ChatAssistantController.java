package sk.tany.rest.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.service.chat.OrderAssistant;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat Assistant", description = "Endpoints for AI Chat Assistant")
public class ChatAssistantController {

    private final OrderAssistant orderAssistant;

    @PostMapping
    @Operation(summary = "Chat with the AI assistant")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String response = orderAssistant.chat(request.message());
        return new ChatResponse(response);
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String response) {}
}
