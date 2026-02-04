package sk.tany.rest.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.dto.isklad.ISkladWebhookRequest;
import sk.tany.rest.api.service.isklad.ISkladWebhookService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/isklad")
@RequiredArgsConstructor
@Slf4j
public class ISkladWebhookController {
    private final ISkladWebhookService webhookService;
    private final ISkladProperties iskladProperties;

    @PostMapping("/order-status")
    public ResponseEntity<?> handleOrderStatusUpdate(@RequestBody ISkladWebhookRequest webhookRequest) {
        // Validate Auth
        if (webhookRequest.getAuth() == null ||
            iskladProperties.getAuthId() == null ||
            !iskladProperties.getAuthId().equals(webhookRequest.getAuth().getAuthId()) ||
            iskladProperties.getIncomingApiKey() == null ||
            !iskladProperties.getIncomingApiKey().equals(webhookRequest.getAuth().getAuthKey())) {

            log.warn("Unauthorized iSklad webhook attempt. AuthID: {}, AuthKey: {}",
                    webhookRequest.getAuth() != null ? webhookRequest.getAuth().getAuthId() : "null",
                    webhookRequest.getAuth() != null ? "provided" : "null");

            Map<String, Object> response = new HashMap<>();
            response.put("auth_status", 0); // 0 or specific error code? Docs say "result code of the authorization". 1 is success.
            return ResponseEntity.status(HttpStatus.OK).body(response); // Usually 200 OK with error payload for these integrations, or 401.
            // Docs say: "The response codes correspond to the code in the Response and error codes section."
            // But usually HTTP 200 is expected for the transport even if logical failure.
            // I'll return 200 with auth_status 0 if it fails auth?
            // "auth_status": (integer) "the result code of the authorization".
            // If I return 401, they might retry. If I return 200 with error status, they might stop.
            // I'll return 200 OK with auth_status 0 (assuming 0 is failure).
        }

        if (webhookRequest.getRequest() == null ||
            !"WriteOrderStatus".equalsIgnoreCase(webhookRequest.getRequest().getReqMethod())) {
             return ResponseEntity.badRequest().body("Invalid request method or data");
        }

        try {
            webhookService.updateOrderStatus(webhookRequest.getRequest().getReqData());
        } catch (Exception e) {
            log.error("Error processing iSklad webhook", e);
             Map<String, Object> response = new HashMap<>();
            response.put("auth_status", 1);
            Map<String, Object> innerResponse = new HashMap<>();
            innerResponse.put("resp_status", 0); // Error
            response.put("response", innerResponse);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("auth_status", 1);
        Map<String, Object> innerResponse = new HashMap<>();
        innerResponse.put("resp_status", 1); // Success
        response.put("response", innerResponse);

        return ResponseEntity.ok(response);
    }
}
