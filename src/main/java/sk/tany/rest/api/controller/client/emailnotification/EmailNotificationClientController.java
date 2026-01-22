package sk.tany.rest.api.controller.client.emailnotification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.client.emailnotification.EmailNotificationCreateRequest;
import sk.tany.rest.api.service.client.emailnotification.EmailNotificationClientService;

@RestController
@RequestMapping("/client/email-notifications")
@RequiredArgsConstructor
@Tag(name = "Email Notification", description = "Endpoints for managing email notifications")
public class EmailNotificationClientController {

    private final EmailNotificationClientService emailNotificationClientService;

    @PostMapping
    @Operation(summary = "Create email notification", description = "Creates a new email notification for a product back in stock.")
    public void createNotification(@RequestBody EmailNotificationCreateRequest request) {
        emailNotificationClientService.createNotification(request);
    }
}
