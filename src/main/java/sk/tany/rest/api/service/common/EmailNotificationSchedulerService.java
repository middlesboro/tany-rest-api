package sk.tany.rest.api.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import sk.tany.rest.api.domain.emailnotification.EmailNotification;
import sk.tany.rest.api.domain.emailnotification.EmailNotificationRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationSchedulerService {

    private final EmailNotificationRepository emailNotificationRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    @Scheduled(cron = "0 */30 * * * *")
    public void processBackInStockNotifications() {
        log.info("Starting Back In Stock notification process...");
        List<EmailNotification> notifications = emailNotificationRepository.findAll();
        if (notifications.isEmpty()) {
            log.info("No pending notifications found.");
            return;
        }

        Set<String> productIds = notifications.stream()
                .map(EmailNotification::getProductId)
                .collect(Collectors.toSet());

        List<Product> products = productRepository.findAllById(productIds);
        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Map<String, List<EmailNotification>> notificationsByEmail = notifications.stream()
                .collect(Collectors.groupingBy(EmailNotification::getEmail));

        notificationsByEmail.forEach((email, userNotifications) -> {
            List<Product> availableProducts = new ArrayList<>();
            List<String> processedNotificationIds = new ArrayList<>();

            for (EmailNotification notification : userNotifications) {
                Product product = productMap.get(notification.getProductId());
                // Check if product exists, is active, and has stock > 0
                if (product != null && product.isActive() && product.getQuantity() != null && product.getQuantity() > 0) {
                    availableProducts.add(product);
                    processedNotificationIds.add(notification.getId());
                }
            }

            if (!availableProducts.isEmpty()) {
                sendEmail(email, availableProducts);
                // Remove processed notifications
                processedNotificationIds.forEach(emailNotificationRepository::deleteById);
                log.info("Sent email to {} for {} products and removed {} notifications.", email, availableProducts.size(), processedNotificationIds.size());
            }
        });
        log.info("Back In Stock notification process finished.");
    }

    private void sendEmail(String to, List<Product> products) {
        try {
            String template = loadTemplate();
            StringBuilder productsListHtml = new StringBuilder();
            for (Product product : products) {
                java.math.BigDecimal price = product.getDiscountPrice() != null && product.getDiscountPrice().compareTo(java.math.BigDecimal.ZERO) >= 0
                        ? product.getDiscountPrice()
                        : product.getPrice();
                String productUrl = frontendUrl + "/products/" + product.getSlug();
                productsListHtml.append("<tr>")
                        .append("<td><a href='").append(productUrl).append("'>").append(product.getTitle()).append("</a></td>")
                        .append("<td>").append(price).append(" €</td>")
                        .append("</tr>");
            }

            String body = template.replace("{{PRODUCTS_LIST}}", productsListHtml.toString());
            emailService.sendEmail(to, "Products Back In Stock!", body, true, null);

        } catch (Exception e) {
            log.error("Failed to send back-in-stock email to {}", to, e);
        }
    }

    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/email/back_in_stock.html");
        byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
        return new String(data, StandardCharsets.UTF_8);
    }
}
