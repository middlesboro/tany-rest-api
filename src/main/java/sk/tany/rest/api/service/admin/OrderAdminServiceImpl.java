package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import sk.tany.rest.api.config.ISkladProperties;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierPriceRange;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cartdiscount.CartDiscount;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.domain.cartdiscount.DiscountType;
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
import sk.tany.rest.api.dto.PriceBreakDown;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.mapper.ISkladMapper;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;
import sk.tany.rest.api.service.isklad.ISkladService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final ISkladService iskladService;
    private final ISkladProperties iskladProperties;
    private final ISkladMapper iskladMapper;

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

        PriceBreakDown breakdown = new PriceBreakDown();
        breakdown.setItems(new ArrayList<>());
        orderDto.setPriceBreakDown(breakdown);

        BigDecimal totalWeight = BigDecimal.ZERO;

        // Products Totals
        BigDecimal productsTotal = BigDecimal.ZERO;
        BigDecimal productsTotalWithoutVat = BigDecimal.ZERO;
        BigDecimal productsTotalVat = BigDecimal.ZERO;

        Map<String, Product> productMap = new HashMap<>();

        // 1. Process Items
        for (OrderItemDto item : items) {
            Product product = productRepository.findById(item.getId()).orElse(null);
            if (product != null) {
                productMap.put(product.getId(), product);

                item.setName(product.getTitle());
                item.setImage(product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().getFirst() : null);
                item.setSlug(product.getSlug());

                BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

                // Prices from product
                BigDecimal effectivePrice = product.getPrice();
                BigDecimal effectivePriceWithoutVat = product.getPriceWithoutVat() != null ? product.getPriceWithoutVat() : product.getPrice();

                // Set item price
                item.setPrice(effectivePrice);

                if (product.getWeight() != null) {
                    totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                }

                // Calculate Totals
                BigDecimal priceWithVat = effectivePrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
                BigDecimal priceWithoutVat = effectivePriceWithoutVat.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
                BigDecimal vatValue = priceWithVat.subtract(priceWithoutVat).setScale(2, RoundingMode.HALF_UP);

                productsTotal = productsTotal.add(priceWithVat);
                productsTotalWithoutVat = productsTotalWithoutVat.add(priceWithoutVat);
                productsTotalVat = productsTotalVat.add(vatValue);

                // Add to breakdown
                PriceItem pi = new PriceItem();
                pi.setType(PriceItemType.PRODUCT);
                pi.setId(product.getId());
                pi.setName(product.getTitle());
                pi.setQuantity(item.getQuantity());
                pi.setImage(item.getImage());
                pi.setPriceWithVat(priceWithVat);
                pi.setPriceWithoutVat(priceWithoutVat);
                pi.setVatValue(vatValue);
                breakdown.getItems().add(pi);
            }
        }
        orderDto.setProductsPrice(productsTotal);
        orderDto.setItems(items);

        // 2. Process Discounts
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<String> appliedCodes = new ArrayList<>();
        boolean freeShipping = false;

        List<CartDiscount> discountsToApply = new ArrayList<>();
        if (orderDto.getCartDiscountIds() != null && !orderDto.getCartDiscountIds().isEmpty()) {
            for (String discountId : orderDto.getCartDiscountIds()) {
                cartDiscountRepository.findById(discountId).ifPresent(discountsToApply::add);
            }
        }

        // Apply Discounts
        for (CartDiscount discount : discountsToApply) {
            boolean isApplicable = false;
            BigDecimal discountAmount = BigDecimal.ZERO;

            boolean hasCategory = discount.getCategoryIds() != null && !discount.getCategoryIds().isEmpty();
            boolean hasProductRestriction = discount.getProductIds() != null && !discount.getProductIds().isEmpty();
            boolean hasBrandRestriction = discount.getBrandIds() != null && !discount.getBrandIds().isEmpty();
            boolean global = !hasCategory && !hasProductRestriction && !hasBrandRestriction;

            // Check applicability per item
            for (OrderItemDto item : items) {
                Product p = productMap.get(item.getId());
                if (p == null) continue;

                boolean itemApplicable = false;
                if (global) {
                    itemApplicable = true;
                } else {
                    if (hasProductRestriction && discount.getProductIds().contains(p.getId())) itemApplicable = true;
                    if (!itemApplicable && hasCategory && p.getCategoryIds() != null && p.getCategoryIds().stream().anyMatch(cid -> discount.getCategoryIds().contains(cid))) itemApplicable = true;
                    if (!itemApplicable && hasBrandRestriction && p.getBrandId() != null && discount.getBrandIds().contains(p.getBrandId())) itemApplicable = true;
                }

                if (itemApplicable) {
                    isApplicable = true;
                    if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
                        BigDecimal itemPrice = p.getPrice();
                        BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                        BigDecimal amount = itemTotal.multiply(discount.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        discountAmount = discountAmount.add(amount);
                    }
                }
            }

            if (isApplicable) {
                 if (discount.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                    BigDecimal limit = BigDecimal.ZERO;
                    for (OrderItemDto item : items) {
                        Product p = productMap.get(item.getId());
                         if (p == null) continue;
                        boolean itemApplicable = false;
                        if (global) itemApplicable = true;
                        else {
                            if (hasProductRestriction && discount.getProductIds().contains(p.getId())) itemApplicable = true;
                            if (!itemApplicable && hasCategory && p.getCategoryIds() != null && p.getCategoryIds().stream().anyMatch(cid -> discount.getCategoryIds().contains(cid))) itemApplicable = true;
                            if (!itemApplicable && hasBrandRestriction && p.getBrandId() != null && discount.getBrandIds().contains(p.getBrandId())) itemApplicable = true;
                        }
                        if (itemApplicable) {
                            limit = limit.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                        }
                    }
                    discountAmount = discount.getValue().min(limit);
                 }

                 if (discount.getDiscountType() == DiscountType.FREE_SHIPPING) {
                     freeShipping = true;
                 }

                 if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                     totalDiscount = totalDiscount.add(discountAmount);
                     if (discount.getCode() != null) appliedCodes.add(discount.getCode());

                     BigDecimal discWithVat = discountAmount.negate().setScale(2, RoundingMode.HALF_UP);
                     // Approximate VAT split for discount
                     BigDecimal totalVatRatio = productsTotalWithoutVat.compareTo(BigDecimal.ZERO) > 0
                             ? productsTotal.divide(productsTotalWithoutVat, 4, RoundingMode.HALF_UP)
                             : BigDecimal.ONE;
                     BigDecimal discWithoutVat = discWithVat.divide(totalVatRatio, 2, RoundingMode.HALF_UP);
                     BigDecimal discVatValue = discWithVat.subtract(discWithoutVat);

                     breakdown.getItems().add(new PriceItem(PriceItemType.DISCOUNT, discount.getId(), discount.getTitle() != null ? discount.getTitle() : discount.getCode(), 1, discWithVat, discWithoutVat, discVatValue));
                 } else if (discount.getDiscountType() == DiscountType.FREE_SHIPPING) {
                      if (discount.getCode() != null) appliedCodes.add(discount.getCode());
                 }
            }
        }

        if (totalDiscount.compareTo(productsTotal) > 0) {
            totalDiscount = productsTotal;
        }
        orderDto.setDiscountPrice(totalDiscount);
        orderDto.setAppliedDiscountCodes(appliedCodes);

        // 3. Process Carrier
        BigDecimal carrierPrice = BigDecimal.ZERO;
        Carrier carrier = null;
        if (orderDto.getCarrierId() != null) {
            carrier = carrierRepository.findById(orderDto.getCarrierId()).orElse(null);
            if (carrier != null) {
                final BigDecimal finalWeight = totalWeight;
                if (carrier.getRanges() == null) {
                    carrier.setRanges(new ArrayList<>());
                }
                CarrierPriceRange range = carrier.getRanges().stream()
                        .filter(r -> (r.getWeightFrom() == null || finalWeight.compareTo(r.getWeightFrom()) >= 0) &&
                                     (r.getWeightTo() == null || finalWeight.compareTo(r.getWeightTo()) <= 0))
                        .findFirst()
                        .orElse(null);

                BigDecimal rangePrice = BigDecimal.ZERO;
                BigDecimal rangePriceWithoutVat = BigDecimal.ZERO;
                BigDecimal rangeVatValue = BigDecimal.ZERO;

                if (range != null) {
                    boolean thresholdMet = range.getFreeShippingThreshold() != null &&
                            productsTotal.subtract(totalDiscount).compareTo(range.getFreeShippingThreshold()) >= 0;
                    boolean isFreeShipping = freeShipping || thresholdMet;

                    if (!isFreeShipping) {
                        rangePrice = range.getPrice() != null ? range.getPrice() : BigDecimal.ZERO;
                        rangePriceWithoutVat = range.getPriceWithoutVat() != null ? range.getPriceWithoutVat() : BigDecimal.ZERO;
                        rangeVatValue = range.getVatValue() != null ? range.getVatValue() : BigDecimal.ZERO;
                    }
                } else {
                     log.warn("No carrier price range found for carrier {} and weight {}. Defaulting to 0.", carrier.getName(), totalWeight);
                }

                carrierPrice = rangePrice;
                orderDto.setCarrierName(carrier.getName());
                orderDto.setCarrierType(carrier.getType());

                PriceItem pi = new PriceItem();
                pi.setType(PriceItemType.CARRIER);
                pi.setName(carrier.getName());
                pi.setPriceWithVat(rangePrice);
                pi.setPriceWithoutVat(rangePriceWithoutVat);
                pi.setVatValue(rangeVatValue);
                pi.setQuantity(1);
                breakdown.getItems().add(pi);
            }
        }
        orderDto.setDeliveryPrice(carrierPrice);

        // 4. Process Payment
        BigDecimal paymentPrice = BigDecimal.ZERO;
        Payment payment = null;
        if (orderDto.getPaymentId() != null) {
             payment = paymentRepository.findById(orderDto.getPaymentId()).orElse(null);
            if (payment != null) {
                paymentPrice = payment.getPrice() != null ? payment.getPrice() : BigDecimal.ZERO;
                BigDecimal paymentPriceWithoutVat = payment.getPriceWithoutVat() != null ? payment.getPriceWithoutVat() : BigDecimal.ZERO;
                BigDecimal paymentVatValue = payment.getVatValue() != null ? payment.getVatValue() : BigDecimal.ZERO;

                orderDto.setPaymentName(payment.getName());
                orderDto.setPaymentType(payment.getType());

                PriceItem pi = new PriceItem();
                pi.setType(PriceItemType.PAYMENT);
                pi.setName(payment.getName());
                pi.setPriceWithVat(paymentPrice);
                pi.setPriceWithoutVat(paymentPriceWithoutVat);
                pi.setVatValue(paymentVatValue);
                pi.setQuantity(1);
                breakdown.getItems().add(pi);
            }
        }

        // 5. Final Price & Breakdown Totals
        BigDecimal finalPrice = productsTotal.add(carrierPrice).add(paymentPrice).subtract(totalDiscount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) finalPrice = BigDecimal.ZERO;
        orderDto.setFinalPrice(finalPrice);

        BigDecimal totalWithVat = BigDecimal.ZERO;
        BigDecimal totalWithoutVat = BigDecimal.ZERO;
        BigDecimal totalVatValue = BigDecimal.ZERO;

        for (PriceItem item : breakdown.getItems()) {
            if (item.getPriceWithVat() != null) totalWithVat = totalWithVat.add(item.getPriceWithVat());
            if (item.getPriceWithoutVat() != null) totalWithoutVat = totalWithoutVat.add(item.getPriceWithoutVat());
            if (item.getVatValue() != null) totalVatValue = totalVatValue.add(item.getVatValue());
        }

        breakdown.setTotalPrice(totalWithVat);
        breakdown.setTotalPriceWithoutVat(totalWithoutVat);
        breakdown.setTotalPriceVatValue(totalVatValue);

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

        if (order.getCancelDate() == null) {
            order.setCancelDate(existingOrder.getCancelDate());
        }
        if (order.getCreditNoteIdentifier() == null) {
            order.setCreditNoteIdentifier(existingOrder.getCreditNoteIdentifier());
        }
        if (order.getIskladImportDate() == null) {
            order.setIskladImportDate(existingOrder.getIskladImportDate());
        }

        if (order.getStatus() != oldStatus) {
            order.getStatusHistory().add(new OrderStatusHistory(order.getStatus(), Instant.now()));
        }

        if (order.getStatus() == OrderStatus.CANCELED) {
            if (order.getCancelDate() == null) {
                order.setCancelDate(Instant.now());
            }
            if (order.getCreditNoteIdentifier() == null) {
                order.setCreditNoteIdentifier(sequenceService.getNextSequence("credit_note_identifier"));
            }
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

        if (order.getStatus() == OrderStatus.CANCELED) {
            if (order.getCancelDate() == null) {
                order.setCancelDate(Instant.now());
            }
            if (order.getCreditNoteIdentifier() == null) {
                order.setCreditNoteIdentifier(sequenceService.getNextSequence("credit_note_identifier"));
            }
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

    @Override
    public void exportToIsklad(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        processIskladExport(order);
    }

    private void processIskladExport(Order order) {
        if (order.getIskladImportDate() == null) {
            try {
                iskladService.createNewOrder(iskladMapper.toCreateNewOrderRequest(orderMapper.toDto(order)));
                order.setIskladImportDate(Instant.now());
                orderRepository.save(order);
            } catch (Exception e) {
                log.error("Failed to create order in iSklad for orderIdentifier {}", order.getOrderIdentifier(), e);
            }
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
