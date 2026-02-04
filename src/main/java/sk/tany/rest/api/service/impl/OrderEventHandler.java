package sk.tany.rest.api.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.event.OrderStatusChangedEvent;
import sk.tany.rest.api.service.common.EmailService;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventHandler {

    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    private String emailTemplate;
    private String emailPaidTemplate;

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        if (order == null) return;

        OrderStatus status = order.getStatus();

        if (order.getStatusHistory() == null) return;

        // Get the latest history entry for the current status
        OrderStatusHistory historyEntry = order.getStatusHistory().stream()
                .filter(h -> h.getStatus() == status)
                .reduce((first, second) -> second)
                .orElse(null);

        if (historyEntry == null) return;

        if (Boolean.TRUE.equals(historyEntry.getEmailSent())) {
            return;
        }

        boolean emailSent = false;
        if (status == OrderStatus.SENT) {
            sendOrderSentEmail(order);
            emailSent = true;
        } else if (status == OrderStatus.PAID) {
            sendOrderPaidEmail(order);
            emailSent = true;
        }

        if (emailSent) {
            historyEntry.setEmailSent(true);
            orderRepository.save(order);
        }
    }

    private void sendOrderSentEmail(Order order) {
        if (order.getEmail() == null || order.getEmail().isEmpty()) {
            log.warn("Cannot send 'Order Sent' email: Customer email is missing for order {}", order.getOrderIdentifier());
            return;
        }
        try {
            String template = getEmailTemplate();

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Customer";
            String orderIdentifier = order.getOrderIdentifier() != null ? order.getOrderIdentifier().toString() : "";
            String carrierLink = order.getCarrierOrderStateLink() != null ? order.getCarrierOrderStateLink() : "#";

            String body = template
                    .replace("{{firstname}}", firstname)
                    .replace("{{orderIdentifier}}", orderIdentifier)
                    .replace("{{carrierOrderStateLink}}", carrierLink)
                    .replace("{{currentYear}}", String.valueOf(java.time.Year.now().getValue()));

            emailService.sendEmail(order.getEmail(), "Objednávka odoslaná", body, true, null);
            log.info("Sent 'Order Sent' email for order {}", order.getOrderIdentifier());

        } catch (Exception e) {
            log.error("Failed to send 'Order Sent' email for order {}", order.getOrderIdentifier(), e);
        }
    }

    private String getEmailTemplate() throws java.io.IOException {
        if (emailTemplate == null) {
            ClassPathResource resource = new ClassPathResource("templates/email/order_sent.html");
            byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
            emailTemplate = new String(data, StandardCharsets.UTF_8);
        }
        return emailTemplate;
    }

    private void sendOrderPaidEmail(Order order) {
        if (order.getEmail() == null || order.getEmail().isEmpty()) {
            log.warn("Cannot send 'Order Paid' email: Customer email is missing for order {}", order.getOrderIdentifier());
            return;
        }
        try {
            String template = getEmailPaidTemplate();

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Customer";
            String orderIdentifier = order.getOrderIdentifier() != null ? order.getOrderIdentifier().toString() : "";
            String orderConfirmationLink = frontendUrl + "/order/confirmation/" + order.getId();

            String body = template
                    .replace("{{firstname}}", firstname)
                    .replace("{{orderIdentifier}}", orderIdentifier)
                    .replace("{{orderConfirmationLink}}", orderConfirmationLink)
                    .replace("{{currentYear}}", String.valueOf(java.time.Year.now().getValue()));

            emailService.sendEmail(order.getEmail(), "Objednávka zaplatená", body, true, null);
            log.info("Sent 'Order Paid' email for order {}", order.getOrderIdentifier());

        } catch (Exception e) {
            log.error("Failed to send 'Order Paid' email for order {}", order.getOrderIdentifier(), e);
        }
    }

    private String getEmailPaidTemplate() throws java.io.IOException {
        if (emailPaidTemplate == null) {
            ClassPathResource resource = new ClassPathResource("templates/email/order_paid.html");
            byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
            emailPaidTemplate = new String(data, StandardCharsets.UTF_8);
        }
        return emailPaidTemplate;
    }
}
