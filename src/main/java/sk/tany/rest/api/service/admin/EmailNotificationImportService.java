package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.emailnotification.EmailNotification;
import sk.tany.rest.api.domain.emailnotification.EmailNotificationRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.admin.import_email_notification.EmailNotificationImportDataDto;
import sk.tany.rest.api.dto.admin.import_email_notification.EmailNotificationImportEntryDto;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationImportService {

    private final EmailNotificationRepository emailNotificationRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public void importEmailNotifications() {
        try {
            ClassPathResource resource = new ClassPathResource("mail_alerts.json");
            InputStream inputStream = resource.getInputStream();
            List<EmailNotificationImportEntryDto> entries = objectMapper.readValue(inputStream, new TypeReference<List<EmailNotificationImportEntryDto>>() {});

            Optional<EmailNotificationImportEntryDto> tableEntry = entries.stream()
                    .filter(e -> "table".equals(e.getType()) && "ps_mailalert_customer_oos".equals(e.getName()))
                    .findFirst();

            if (tableEntry.isPresent()) {
                List<EmailNotificationImportDataDto> data = tableEntry.get().getData();
                if (data == null || data.isEmpty()) {
                    log.warn("No data found in mail_alerts.json");
                    return;
                }

                for (EmailNotificationImportDataDto dto : data) {
                    processEmailNotification(dto);
                }
            } else {
                log.warn("Table ps_mailalert_customer_oos not found in mail_alerts.json");
            }
        } catch (Exception e) {
            log.error("Failed to import email notifications from mail_alerts.json", e);
        }
    }

    private void processEmailNotification(EmailNotificationImportDataDto dto) {
        if (dto.getIdProduct() == null || dto.getCustomerEmail() == null) {
            return;
        }

        try {
            Long productIdentifier = Long.parseLong(dto.getIdProduct());
            Optional<Product> productOpt = productRepository.findByProductIdentifier(productIdentifier);

            if (productOpt.isPresent()) {
                Product product = productOpt.get();

                // check if it already exists
                Optional<EmailNotification> existing = emailNotificationRepository.findByEmailAndProductId(dto.getCustomerEmail(), product.getId());
                if (existing.isEmpty()) {
                    EmailNotification emailNotification = new EmailNotification();
                    emailNotification.setEmail(dto.getCustomerEmail());
                    emailNotification.setProductId(product.getId());
                    emailNotificationRepository.save(emailNotification);
                    log.info("Imported email notification for email {} and product {}", dto.getCustomerEmail(), product.getId());
                } else {
                    log.info("Email notification for email {} and product {} already exists", dto.getCustomerEmail(), product.getId());
                }

            } else {
                log.warn("Product with identifier {} not found. Skipping email notification import for email {}", productIdentifier, dto.getCustomerEmail());
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid product identifier format: {}. Skipping...", dto.getIdProduct());
        }
    }
}
