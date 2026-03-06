package sk.tany.rest.api.service.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.config.BrevoConfig;
import sk.tany.rest.api.domain.mailplatform.MailPlatformType;
import sk.tany.rest.api.exception.EmailException;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("brevoEmailService")
@RequiredArgsConstructor
@Slf4j
public class BrevoEmailService implements PlatformEmailService {

    private final BrevoConfig brevoConfig;
    private final ObjectMapper objectMapper;
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public MailPlatformType getPlatformType() {
        return MailPlatformType.BREVO;
    }

    @Override
    public void sendEmail(String to, String subject, String body, boolean isHtml, File... attachments) {
        try {
            Map<String, Object> payload = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("email", brevoConfig.getFromEmail());
            sender.put("name", brevoConfig.getFromName());
            payload.put("sender", sender);

            Map<String, String> toRecipient = new HashMap<>();
            toRecipient.put("email", to);
            payload.put("to", Collections.singletonList(toRecipient));

            if (subject != null && !subject.endsWith(" - Tany.sk")) {
                subject = subject + " - Tany.sk";
            }
            payload.put("subject", subject);

            if (isHtml) {
                payload.put("htmlContent", body);
            } else {
                payload.put("textContent", body);
            }

            if (attachments != null && attachments.length > 0) {
                List<Map<String, String>> emailAttachments = new ArrayList<>();
                for (File file : attachments) {
                    if (file != null && file.exists()) {
                        try {
                            byte[] fileContent = Files.readAllBytes(file.toPath());
                            Map<String, String> attachment = new HashMap<>();
                            attachment.put("name", file.getName());
                            attachment.put("content", Base64.getEncoder().encodeToString(fileContent));
                            emailAttachments.add(attachment);
                        } catch (Exception e) {
                            throw new EmailException("Failed to attach file for Brevo: " + file.getName(), e);
                        }
                    }
                }
                if (!emailAttachments.isEmpty()) {
                    payload.put("attachment", emailAttachments);
                }
            }

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", brevoConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Brevo email sent successfully. Response: {}", response.body());
            } else {
                log.error("Failed to send Brevo email. Status code: {}, Response: {}", response.statusCode(), response.body());
                throw new EmailException("Failed to send email via Brevo. Status code: " + response.statusCode());
            }

        } catch (EmailException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send Brevo email", e);
            throw new EmailException("Failed to send email via Brevo", e);
        }
    }
}
