package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.common.EmailService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAdminServiceImpl implements OrderAdminService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${eshop.frontend-url}")
    private String frontendUrl;

    private String emailTemplate;
    private String emailPaidTemplate;

    @Override
    public Page<OrderDto> findAll(Long orderIdentifier, OrderStatus status, BigDecimal priceFrom, BigDecimal priceTo, String carrierId, String paymentId, Instant createDateFrom, Instant createDateTo, Pageable pageable) {
        return orderRepository.findAll(orderIdentifier, status, priceFrom, priceTo, carrierId, paymentId, createDateFrom, createDateTo, pageable).map(orderMapper::toDto);
    }

    @Override
    public Optional<OrderDto> findById(String id) {
        return orderRepository.findById(id).map(orderMapper::toDto);
    }

    @Override
    public OrderDto save(OrderDto orderDto) {
        var order = orderMapper.toEntity(orderDto);
        if (order.getId() == null) {
            if (order.getStatus() == null) {
                order.setStatus(OrderStatus.CREATED);
            }
            if (order.getStatusHistory() == null) {
                order.setStatusHistory(new ArrayList<>());
            }
            order.getStatusHistory().add(new OrderStatusHistory(order.getStatus(), Instant.now()));
        }
        var savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderDto update(String id, OrderDto orderDto) {
        orderDto.setId(id);
        Order existingOrder = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderStatus oldStatus = existingOrder.getStatus();

        if (orderDto.getStatus() == null) {
            orderDto.setStatus(oldStatus);
        }

        var order = orderMapper.toEntity(orderDto);
        order.setStatusHistory(existingOrder.getStatusHistory());
        if (order.getStatusHistory() == null) {
            order.setStatusHistory(new ArrayList<>());
        }

        if (order.getStatus() != oldStatus) {
            order.getStatusHistory().add(new OrderStatusHistory(order.getStatus(), Instant.now()));
        }

        var savedOrder = orderRepository.save(order);

        if (savedOrder.getStatus() == OrderStatus.SENT && oldStatus != OrderStatus.SENT) {
            sendOrderSentEmail(savedOrder);
        } else if (savedOrder.getStatus() == OrderStatus.PAID && oldStatus != OrderStatus.PAID) {
            sendOrderPaidEmail(savedOrder);
        }

        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderDto patch(String id, sk.tany.rest.api.dto.admin.order.patch.OrderPatchRequest patchDto) {
        var order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        OrderStatus oldStatus = order.getStatus();
        orderMapper.updateEntityFromPatch(patchDto, order);

        if (order.getStatus() != oldStatus) {
            if (order.getStatusHistory() == null) {
                order.setStatusHistory(new ArrayList<>());
            }
            order.getStatusHistory().add(new OrderStatusHistory(order.getStatus(), Instant.now()));
        }

        var savedOrder = orderRepository.save(order);

        if (savedOrder.getStatus() == OrderStatus.SENT && oldStatus != OrderStatus.SENT) {
            sendOrderSentEmail(savedOrder);
        } else if (savedOrder.getStatus() == OrderStatus.PAID && oldStatus != OrderStatus.PAID) {
            sendOrderPaidEmail(savedOrder);
        }

        return orderMapper.toDto(savedOrder);
    }

    @Override
    public void deleteById(String id) {
        orderRepository.deleteById(id);
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
                    .replace("{{carrierOrderStateLink}}", carrierLink);

            emailService.sendEmail(order.getEmail(), "Order Shipped", body, true, null);
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
                    .replace("{{orderConfirmationLink}}", orderConfirmationLink);

            emailService.sendEmail(order.getEmail(), "Order Paid", body, true, null);
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
