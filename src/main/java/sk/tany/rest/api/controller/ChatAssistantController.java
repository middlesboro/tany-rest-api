package sk.tany.rest.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.client.customermessage.CustomerMessageCreateRequest;
import sk.tany.rest.api.client.TanyFeaturesClient;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatAssistantController {

    private final TanyFeaturesClient tanyFeaturesClient;

    @PostMapping("/message")
    public ResponseEntity<String> chatMessage(@Valid @RequestBody CustomerMessageCreateRequest request) {
        try {
            String response = tanyFeaturesClient.chatMessage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to communicate with tany-features AI assistant", e);
            return ResponseEntity.internalServerError().body("Sorry, the assistant is currently unavailable.");
        }
    }
}
