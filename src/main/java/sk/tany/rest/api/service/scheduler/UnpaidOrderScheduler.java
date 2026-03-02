package sk.tany.rest.api.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.util.HtmlUtils;
import sk.tany.rest.api.config.EshopConfig;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.service.common.EmailService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnpaidOrderScheduler {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ShopSettingsRepository shopSettingsRepository;
    private final EmailService emailService;
    private final EshopConfig eshopConfig;

    @Scheduled(cron = "0 */30 * * * *")
    public void checkUnpaidOrders() {
        log.info("Checking for unpaid orders...");

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Order> orders = orderRepository.findAllByStatusAndCreateDateAfterAndPaymentNotificationDateIsNull(OrderStatus.CREATED, thirtyDaysAgo);

        if (orders.isEmpty()) return;

        ShopSettings settings = shopSettingsRepository.getFirstShopSettings();

        for (Order order : orders) {
            processOrder(order, settings);
        }
    }

    private void processOrder(Order order, ShopSettings settings) {
        if (order.getPaymentId() == null) return;

        Payment payment = paymentRepository.findById(order.getPaymentId()).orElse(null);
        if (payment == null) return;

        Instant now = Instant.now();
        Instant createDate = order.getCreateDate();
        if (createDate == null) return;

        long minutesSinceCreation = ChronoUnit.MINUTES.between(createDate, now);

        boolean shouldSend = false;
        String templateName = null;
        String subject = "Nezaplatená objednávka č. " + order.getOrderIdentifier();

        if (payment.getType() == PaymentType.BANK_TRANSFER) {
            if (minutesSinceCreation >= 48 * 60) { // 48 hours
                shouldSend = true;
                templateName = "templates/email/order_unpaid_bank.html";
            }
        } else if (payment.getType() == PaymentType.BESTERON || payment.getType() == PaymentType.GLOBAL_PAYMENTS) {
            if (minutesSinceCreation >= 30) { // 30 minutes
                shouldSend = true;
                templateName = "templates/email/order_unpaid_online.html";
            }
        }

        if (shouldSend && templateName != null) {
            sendEmail(order, templateName, subject, settings);
        }
    }

    private void sendEmail(Order order, String templateName, String subject, ShopSettings settings) {
        if (order.getEmail() == null || order.getEmail().isEmpty()) {
            log.warn("Cannot send unpaid order email: Customer email is missing for order {}", order.getOrderIdentifier());
            return;
        }

        try {
            String template = getEmailTemplate(templateName);

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Zákazník";
            String orderIdentifier = order.getOrderIdentifier() != null ? order.getOrderIdentifier().toString() : "";
            String orderConfirmationLink = eshopConfig.getFrontendUrl() + "/order/confirmation/" + order.getId();
            String finalPrice = order.getFinalPrice() != null ? String.format("%.2f&nbsp;€", order.getFinalPrice()) : "0,00&nbsp;€";
            String iban = settings.getBankAccount() != null ? settings.getBankAccount() : "";

            String body = template
                    .replace("{{firstname}}", HtmlUtils.htmlEscape(firstname))
                    .replace("{{orderIdentifier}}", orderIdentifier)
                    .replace("{{finalPrice}}", finalPrice)
                    .replace("{{iban}}", iban)
                    .replace("{{orderConfirmationLink}}", orderConfirmationLink)
                    .replace("{{currentYear}}", String.valueOf(java.time.Year.now().getValue()))
                    .replace("{{supportEmail}}", settings.getShopEmail() != null ? settings.getShopEmail() : "")
                    .replace("{{supportPhone}}", settings.getShopPhoneNumber() != null ? settings.getShopPhoneNumber() : "");

            emailService.sendEmail(order.getEmail(), subject, body, true, null);
            log.info("Sent unpaid order notification for order {}", order.getOrderIdentifier());

            order.setPaymentNotificationDate(Instant.now());
            orderRepository.save(order);

        } catch (Exception e) {
            log.error("Failed to send unpaid order email for order {}", order.getOrderIdentifier(), e);
        }
    }

    private String getEmailTemplate(String template) throws IOException {
        ClassPathResource resource = new ClassPathResource(template);
        byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
        return new String(data, StandardCharsets.UTF_8);
    }
}
