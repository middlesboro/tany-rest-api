package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.service.admin.EmailNotificationImportService;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
public class EmailNotificationImportAdminController {

    private final EmailNotificationImportService emailNotificationImportService;

    @Operation(summary = "Import email notifications from JSON")
    @PostMapping("/email-notifications")
    public void importEmailNotifications() {
        emailNotificationImportService.importEmailNotifications();
    }
}
