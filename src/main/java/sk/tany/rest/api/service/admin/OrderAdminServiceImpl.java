package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierPriceRange;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cartdiscount.CartDiscount;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.OrderStatusHistoryDto;
import sk.tany.rest.api.dto.PriceBreakDown;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAdminServiceImpl implements OrderAdminService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final EmailService emailService;
    private final ProductRepository productRepository;
    private final CarrierRepository carrierRepository;
    private final PaymentRepository paymentRepository;
    private final CartDiscountRepository cartDiscountRepository;
    private final SequenceService sequenceService;

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
        if (orderDto.getId() == null) {
            // New Order Logic
            return createOrder(orderDto);
        } else {
            // Existing Logic
            var order = orderMapper.toEntity(orderDto);
            var savedOrder = orderRepository.save(order);
            return orderMapper.toDto(savedOrder);
        }
    }

    private OrderDto createOrder(OrderDto orderDto) {
        List<OrderItemDto> items = orderDto.getItems();
        if (items == null) items = new ArrayList<>();

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal productsPrice = BigDecimal.ZERO;
        List<PriceItem> priceItems = new ArrayList<>();

        // 1. Process Items
        for (OrderItemDto item : items) {
            Product product = productRepository.findById(item.getId()).orElse(null);
            if (product != null) {
                item.setName(product.getTitle());
                item.setPrice(product.getPrice());
                item.setImage(product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null);
                item.setSlug(product.getSlug());

                BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
                BigDecimal itemPrice = product.getPrice().multiply(quantity);
                productsPrice = productsPrice.add(itemPrice);

                if (product.getWeight() != null) {
                    totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                }

                // Add to breakdown
                PriceItem pi = new PriceItem();
                pi.setType(PriceItemType.PRODUCT);
                pi.setId(product.getId());
                pi.setName(product.getTitle());
                pi.setQuantity(item.getQuantity());
                pi.setPriceWithVat(itemPrice);
                pi.setImage(item.getImage());
                priceItems.add(pi);
            }
        }
        orderDto.setProductsPrice(productsPrice);
        orderDto.setItems(items);

        // 2. Process Carrier
        BigDecimal carrierPrice = BigDecimal.ZERO;
        Carrier carrier = null;
        if (orderDto.getCarrierId() != null) {
            carrier = carrierRepository.findById(orderDto.getCarrierId()).orElse(null);
            if (carrier != null) {
                carrierPrice = calculateCarrierPrice(carrier, totalWeight);
                orderDto.setCarrierName(carrier.getName());
                orderDto.setCarrierType(carrier.getType());

                PriceItem pi = new PriceItem();
                pi.setType(PriceItemType.CARRIER);
                pi.setName(carrier.getName());
                pi.setPriceWithVat(carrierPrice);
                pi.setQuantity(1);
                priceItems.add(pi);
            }
        }
        orderDto.setDeliveryPrice(carrierPrice);

        // 3. Process Payment
        BigDecimal paymentPrice = BigDecimal.ZERO;
        Payment payment = null;
        if (orderDto.getPaymentId() != null) {
             payment = paymentRepository.findById(orderDto.getPaymentId()).orElse(null);
            if (payment != null) {
                paymentPrice = payment.getPrice() != null ? payment.getPrice() : BigDecimal.ZERO;
                orderDto.setPaymentName(payment.getName());
                orderDto.setPaymentType(payment.getType());

                PriceItem pi = new PriceItem();
                pi.setType(PriceItemType.PAYMENT);
                pi.setName(payment.getName());
                pi.setPriceWithVat(paymentPrice);
                pi.setQuantity(1);
                priceItems.add(pi);
            }
        }

        // 4. Process Discounts
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<String> appliedCodes = new ArrayList<>();
        if (orderDto.getCartDiscountIds() != null && !orderDto.getCartDiscountIds().isEmpty()) {
            for (String discountId : orderDto.getCartDiscountIds()) {
                CartDiscount discount = cartDiscountRepository.findById(discountId).orElse(null);
                if (discount != null) {
                    BigDecimal discountAmount = calculateDiscount(discount, productsPrice);
                    totalDiscount = totalDiscount.add(discountAmount);
                    if (discount.getCode() != null) appliedCodes.add(discount.getCode());

                    PriceItem pi = new PriceItem();
                    pi.setType(PriceItemType.DISCOUNT);
                    pi.setName(discount.getTitle() != null ? discount.getTitle() : discount.getCode());
                    pi.setPriceWithVat(discountAmount.negate());
                    pi.setQuantity(1);
                    priceItems.add(pi);
                }
            }
        }
        orderDto.setDiscountPrice(totalDiscount);
        orderDto.setAppliedDiscountCodes(appliedCodes);


        // 5. Final Price & Breakdown
        BigDecimal finalPrice = productsPrice.add(carrierPrice).add(paymentPrice).subtract(totalDiscount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) finalPrice = BigDecimal.ZERO;
        orderDto.setFinalPrice(finalPrice);

        PriceBreakDown breakdown = new PriceBreakDown();
        breakdown.setItems(priceItems);
        breakdown.setTotalPrice(finalPrice);
        orderDto.setPriceBreakDown(breakdown);

        // 6. Meta
        orderDto.setOrderIdentifier(sequenceService.getNextSequence("order_identifier"));
        orderDto.setCreateDate(Instant.now());
        if (orderDto.getStatus() == null) orderDto.setStatus(OrderStatus.CREATED);

        Order order = orderMapper.toEntity(orderDto);
        if (order.getStatusHistory() == null) order.setStatusHistory(new ArrayList<>());
        order.getStatusHistory().add(new OrderStatusHistory(order.getStatus(), Instant.now()));

        // Ensure Authenticated User
        if (order.getCustomerId() != null) {
            order.setAuthenticatedUser(true);
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    private BigDecimal calculateCarrierPrice(Carrier carrier, BigDecimal totalWeight) {
        if (carrier.getRanges() == null || carrier.getRanges().isEmpty()) return BigDecimal.ZERO;

        CarrierPriceRange matchedRange = carrier.getRanges().stream()
                .filter(range -> (range.getWeightFrom() == null || totalWeight.compareTo(range.getWeightFrom()) >= 0) &&
                                 (range.getWeightTo() == null || totalWeight.compareTo(range.getWeightTo()) <= 0))
                .findFirst()
                .orElse(null);

        if (matchedRange != null) {
             return matchedRange.getPrice() != null ? matchedRange.getPrice() : BigDecimal.ZERO;
        }

        log.warn("No carrier price range found for carrier {} and weight {}. Defaulting to 0.", carrier.getName(), totalWeight);
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateDiscount(CartDiscount discount, BigDecimal productsPrice) {
        if (discount.getValue() == null) return BigDecimal.ZERO;

        if (Boolean.TRUE.equals(discount.isPercentage())) {
             return productsPrice.multiply(discount.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
             return discount.getValue();
        }
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
