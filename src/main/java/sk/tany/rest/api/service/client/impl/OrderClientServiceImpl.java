package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.helper.OrderHelper;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.client.OrderClientService;
import sk.tany.rest.api.service.client.ProductClientService;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderClientServiceImpl implements OrderClientService {

    private static final Logger log = LoggerFactory.getLogger(OrderClientServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerRepository customerRepository;
    private final SequenceService sequenceService;
    private final CarrierRepository carrierRepository;
    private final PaymentRepository paymentRepository;
    private final ProductClientService productClientService;
    private final EmailService emailService;
    private final ResourceLoader resourceLoader;

    private String getCurrentCustomerId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerRepository.findByEmail(email)
                .map(Customer::getId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Override
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        order.setSelectedPickupPointId(orderDto.getSelectedPickupPointId());
        order.setSelectedPickupPointName(orderDto.getSelectedPickupPointName());
        try {
            order.setCustomerId(getCurrentCustomerId());
        } catch (Exception e) {
            // nothing to do. if customer not found, order will be created without customerId
        }
        List<ProductDto> products = productClientService.findAllByIds(order.getItems().stream().map(OrderItem::getId).toList());
        order.setProductsPrice(OrderHelper.getProductsPrice(products));

        Optional<Carrier> carrierOptional = carrierRepository.findById(orderDto.getCarrierId());
        if (carrierOptional.isPresent()) {
            Carrier carrier = carrierOptional.get();

            BigDecimal totalWeight = OrderHelper.getProductsWeight(products);
            order.setCarrierPrice(OrderHelper.getCarrierPrice(carrier, totalWeight));
        }

        Optional<Payment> paymentOptional = paymentRepository.findById(orderDto.getPaymentId());
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            order.setPaymentPrice(payment.getPrice());
        }

        // add carrier price and payment price
        order.setOrderIdentifier(sequenceService.getNextSequence("order_identifier"));
        Order savedOrder = orderRepository.save(order);

        savedOrder.getItems().forEach(item -> {
            productClientService.updateProductStock(item.getId(), item.getQuantity());
        });

        sendOrderCreatedEmail(savedOrder);

        return orderMapper.toDto(savedOrder);
    }

    private void sendOrderCreatedEmail(Order order) {
        try {
            Resource templateResource = resourceLoader.getResource("classpath:templates/email/order_created.html");
            String template;
            try (InputStream inputStream = templateResource.getInputStream()) {
                template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Customer";
            template = template.replace("{{firstname}}", StringEscapeUtils.escapeHtml4(firstname));
            template = template.replace("{{orderIdentifier}}", String.valueOf(order.getOrderIdentifier()));

            Resource pdfResource = resourceLoader.getResource("classpath:empty.pdf");
            File pdfFile = File.createTempFile("order_attachment", ".pdf");
            try {
                try (InputStream inputStream = pdfResource.getInputStream()) {
                    Files.copy(inputStream, pdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                emailService.sendEmail(order.getEmail(), "Order Confirmation #" + order.getOrderIdentifier(), template, true, pdfFile);
            } finally {
                // Cleanup temp file
                if (pdfFile != null && pdfFile.exists()) {
                    pdfFile.delete();
                }
            }

        } catch (IOException e) {
            // Log error but do not fail the order creation
            log.error("Failed to send order confirmation email: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage(), e);
        }
    }

    // todo do it e.g. accessible for 1h after creation. otherwise needed authorization. or find better solution
    @Override
    public OrderDto getOrder(String id) {
        return orderRepository.findById(id)
                .filter(order -> order.getCustomerId().equals(getCurrentCustomerId()))
                .map(order -> {
                    OrderDto dto = orderMapper.toDto(order);
                    if (dto.getCarrierId() != null) {
                        carrierRepository.findById(dto.getCarrierId())
                                .ifPresent(carrier -> {
                                    dto.setCarrierType(carrier.getType());
                                    dto.setCarrierName(carrier.getName());
                                });
                    }
                    if (dto.getPaymentId() != null) {
                        paymentRepository.findById(dto.getPaymentId())
                                .ifPresent(payment -> {
                                    dto.setPaymentType(payment.getType());
                                    dto.setPaymentName(payment.getName());
                                });
                    }
                    return dto;
                })
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));
    }
}
