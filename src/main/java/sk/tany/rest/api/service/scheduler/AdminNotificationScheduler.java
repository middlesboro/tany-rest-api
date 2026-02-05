package sk.tany.rest.api.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.customer.Role;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.service.common.EmailService;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationScheduler {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CarrierRepository carrierRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 */30 * * * *")
    public void checkNewOrders() {
        List<Order> newOrders = orderRepository.findAllByAdminNotificationDateIsNull();

        if (newOrders.isEmpty()) {
            return;
        }

        log.info("Found {} new orders to process for admin notification.", newOrders.size());

        List<Customer> admins = customerRepository.findAllByRole(Role.ADMIN);
        if (admins.isEmpty()) {
            log.warn("No ADMIN customer found. Skipping notification.");
            markAsNotified(newOrders);
            return;
        }

        Customer admin = admins.get(0);

        List<Order> validOrders = newOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELED)
                .collect(Collectors.toList());

        if (!validOrders.isEmpty()) {
            String emailBody = buildEmailBody(validOrders);
            emailService.sendEmail(admin.getEmail(), "New Orders Summary", emailBody, true, null);
            log.info("Sent summary email for {} orders to admin {}", validOrders.size(), admin.getEmail());
        }

        markAsNotified(newOrders);
    }

    private void markAsNotified(List<Order> orders) {
        Instant now = Instant.now();
        for (Order order : orders) {
            order.setAdminNotificationDate(now);
            orderRepository.save(order);
        }
    }

    private String buildEmailBody(List<Order> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>New Orders Summary</h1>");

        for (Order order : orders) {
            sb.append("<div style='border: 1px solid #ccc; padding: 10px; margin-bottom: 10px;'>");
            sb.append("<h3>Order #").append(order.getOrderIdentifier()).append("</h3>");
            sb.append("<p><strong>Final Price:</strong> ").append(order.getFinalPrice()).append("</p>");

            String carrierName = "Unknown";
            if (order.getCarrierId() != null) {
                carrierName = carrierRepository.findById(order.getCarrierId())
                        .map(Carrier::getName)
                        .orElse("Unknown");
            }
            sb.append("<p><strong>Carrier:</strong> ").append(carrierName).append("</p>");

            String paymentName = "Unknown";
            if (order.getPaymentId() != null) {
                paymentName = paymentRepository.findById(order.getPaymentId())
                        .map(Payment::getName)
                        .orElse("Unknown");
            }
            sb.append("<p><strong>Payment:</strong> ").append(paymentName).append("</p>");

            sb.append("<p><strong>Products:</strong></p>");
            sb.append("<ul>");
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    sb.append("<li>").append(item.getName())
                      .append(" (").append(item.getQuantity()).append(" pcs)")
                      .append("</li>");
                }
            }
            sb.append("</ul>");
            sb.append("</div>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }
}
