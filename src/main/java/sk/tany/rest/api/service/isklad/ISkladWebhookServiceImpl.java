package sk.tany.rest.api.service.isklad;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.dto.isklad.ISkladOrderStatusUpdateRequest;
import sk.tany.rest.api.dto.isklad.ISkladPackage;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ISkladWebhookServiceImpl implements ISkladWebhookService {

    private final OrderRepository orderRepository;
    private final SequenceService sequenceService;
    private final EmailService emailService;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    private String emailTemplate;

    @Override
    public void updateOrderStatus(ISkladOrderStatusUpdateRequest request) {
        if (request.getOrderOriginalId() == null) {
            log.warn("Received updateOrderStatus request without order_original_id");
            return;
        }

        Long orderIdentifier;
        try {
            orderIdentifier = Long.parseLong(request.getOrderOriginalId());
        } catch (NumberFormatException e) {
            log.error("Invalid order_original_id format: {}", request.getOrderOriginalId());
            return;
        }

        Order order = orderRepository.findByOrderIdentifier(orderIdentifier).orElse(null);
        if (order == null) {
            log.warn("Order not found for orderIdentifier: {}", orderIdentifier);
            return;
        }

        OrderStatus newStatus = mapStatus(request.getStatusId());
        if (newStatus == null) {
            log.debug("Status ID {} not mapped to any OrderStatus, ignoring update.", request.getStatusId());
            return; // Or should we proceed if we just want to update tracking link? User said "we need just change state of order when there is some change."
            // If status is not relevant, maybe we still update tracking?
        }

        boolean changed = false;

        // Update Tracking Link
        if (request.getPackages() != null) {
            for (ISkladPackage pkg : request.getPackages()) {
                if (pkg.getTrackingUrl() != null && !pkg.getTrackingUrl().isEmpty()) {
                    if (!pkg.getTrackingUrl().equals(order.getCarrierOrderStateLink())) {
                        order.setCarrierOrderStateLink(pkg.getTrackingUrl());
                        changed = true;
                    }
                    break; // Use the first one
                }
            }
        }

        // Update Status
        if (order.getStatus() != newStatus) {
            order.setStatus(newStatus);
            if (order.getStatusHistory() == null) {
                order.setStatusHistory(new ArrayList<>());
            }
            order.getStatusHistory().add(new OrderStatusHistory(newStatus, Instant.now()));
            changed = true;

            // Handle CANCELED logic
            if (newStatus == OrderStatus.CANCELED) {
                if (order.getCancelDate() == null) {
                    order.setCancelDate(Instant.now());
                }
                if (order.getCreditNoteIdentifier() == null) {
                    order.setCreditNoteIdentifier(sequenceService.getNextSequence("credit_note_identifier"));
                }
            }
        }

        if (changed) {
            Order savedOrder = orderRepository.save(order);
            log.info("Updated order {} status to {} and tracking link.", order.getOrderIdentifier(), newStatus);

            if (savedOrder.getStatus() == OrderStatus.SENT) {
                sendOrderSentEmail(savedOrder);
            }
        }
    }

    private OrderStatus mapStatus(Integer statusId) {
        if (statusId == null) return null;
        switch (statusId) {
            case 7: return OrderStatus.READY_FOR_PICKUP;
            case 3: return OrderStatus.PACKING;
            case 4: return OrderStatus.PACKED;
            case 5: return OrderStatus.SENT;
            case 6: return OrderStatus.DELIVERED;
            case 15: return OrderStatus.CANCELED;
            default: return null;
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

    private String getEmailTemplate() throws IOException {
        if (emailTemplate == null) {
            ClassPathResource resource = new ClassPathResource("templates/email/order_sent.html");
            byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
            emailTemplate = new String(data, StandardCharsets.UTF_8);
        }
        return emailTemplate;
    }
}
