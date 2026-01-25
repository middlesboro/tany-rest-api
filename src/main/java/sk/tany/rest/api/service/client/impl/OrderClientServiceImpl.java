package sk.tany.rest.api.service.client.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.customer.Address;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.helper.OrderHelper;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.service.admin.InvoiceService;
import sk.tany.rest.api.service.client.OrderClientService;
import sk.tany.rest.api.service.client.ProductClientService;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;
import sk.tany.rest.api.exception.OrderException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final ProductSalesRepository productSalesRepository;
    private final ProductSearchEngine productSearchEngine;
    private final CartRepository cartRepository;
    private final sk.tany.rest.api.service.client.CartClientService cartService;
    private final InvoiceService invoiceService;

    private String cachedTemplate;

    @PostConstruct
    public void init() {
        try {
            Resource templateResource = resourceLoader.getResource("classpath:templates/email/order_created.html");
            try (InputStream inputStream = templateResource.getInputStream()) {
                cachedTemplate = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("Failed to load email template", e);
        }
    }

    private String getCurrentCustomerId() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return customerRepository.findByEmail(email)
                    .map(Customer::getId)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private Address mapAddress(AddressDto dto) {
        if (dto == null) return null;
        return new Address(dto.getStreet(), dto.getCity(), dto.getZip());
    }

    @Override
    public OrderDto createOrder(OrderDto orderDto) {
        if (orderDto.getCartId() == null) {
            throw new OrderException.BadRequest("Cart ID is required to create an order");
        }

        CartDto cartDto = cartService.getOrCreateCart(orderDto.getCartId(), null);
        if (cartDto == null || cartDto.getItems() == null || cartDto.getItems().isEmpty()) {
            throw new OrderException.BadRequest("Cart is empty or not found");
        }

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.CREATED, Instant.now()));
        order.setCartId(orderDto.getCartId());
        order.setNote(orderDto.getNote());
        order.setCustomerId(getCurrentCustomerId());

        // Populate from Cart
        order.setFirstname(cartDto.getFirstname());
        order.setLastname(cartDto.getLastname());
        order.setEmail(cartDto.getEmail());
        order.setPhone(cartDto.getPhone());
        order.setInvoiceAddress(mapAddress(cartDto.getInvoiceAddress()));
        order.setDeliveryAddress(mapAddress(cartDto.getDeliveryAddress()));
        order.setDeliveryAddressSameAsInvoiceAddress(cartDto.isDeliveryAddressSameAsInvoiceAddress());

        order.setCarrierId(cartDto.getSelectedCarrierId());
        order.setPaymentId(cartDto.getSelectedPaymentId());
        order.setSelectedPickupPointId(cartDto.getSelectedPickupPointId());
        order.setSelectedPickupPointName(cartDto.getSelectedPickupPointName());

        // Map Items
        List<OrderItem> orderItems = new ArrayList<>();
        if (cartDto.getItems() != null) {
            for (CartItem ci : cartDto.getItems()) {
                OrderItem oi = new OrderItem();
                oi.setId(ci.getProductId());
                oi.setName(ci.getTitle());
                oi.setQuantity(ci.getQuantity());
                oi.setPrice(ci.getPrice());
                oi.setImage(ci.getImage());
                orderItems.add(oi);
            }
        }
        order.setItems(orderItems);

        // Prices
        order.setProductsPrice(cartDto.getTotalPrice());
        order.setDiscountPrice(cartDto.getTotalDiscount());
        order.setFinalPrice(cartDto.getFinalPrice());
        order.setPriceBreakDown(cartDto.getPriceBreakDown());

        if (cartDto.getAppliedDiscounts() != null) {
            List<String> codes = cartDto.getAppliedDiscounts().stream()
                    .map(sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto::getCode)
                    .collect(Collectors.toList());
            order.setAppliedDiscountCodes(codes);
        }

        Carrier carrier = null;
        if (order.getCarrierId() != null) {
            carrier = carrierRepository.findById(order.getCarrierId()).orElse(null);
        }
        // Extract carrier price from breakdown if possible or calculate?
        // CartDto does not expose carrier price directly as a field except inside breakdown or implicitly in final price.
        // However, we can find it in breakdown.
        if (cartDto.getPriceBreakDown() != null && cartDto.getPriceBreakDown().getItems() != null) {
            BigDecimal carrierPrice = cartDto.getPriceBreakDown().getItems().stream()
                    .filter(i -> i.getType() == sk.tany.rest.api.dto.PriceItemType.CARRIER)
                    .map(sk.tany.rest.api.dto.PriceItem::getPriceWithVat)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            order.setCarrierPrice(carrierPrice);

            BigDecimal paymentPrice = cartDto.getPriceBreakDown().getItems().stream()
                    .filter(i -> i.getType() == sk.tany.rest.api.dto.PriceItemType.PAYMENT)
                    .map(sk.tany.rest.api.dto.PriceItem::getPriceWithVat)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            order.setPaymentPrice(paymentPrice);
        } else {
            // Fallback if breakdown missing (should not happen)
            order.setCarrierPrice(BigDecimal.ZERO);
            order.setPaymentPrice(BigDecimal.ZERO);
        }

        // If free shipping, carrier price should be effectively 0 in final calculation, but breakdown shows real cost usually?
        // Actually CartClientServiceImpl: "if (discountAmount.compareTo(BigDecimal.ZERO) > 0 || discount.isFreeShipping()) ... breakdown.add(...)"
        // It adds carrier price to breakdown. But if free shipping?
        // "if (!freeShipping && cartDto.getSelectedCarrierId() != null) ... add carrier to breakdown"
        // So if freeShipping, carrier is NOT added to breakdown.
        // So carrierPrice will be 0. Correct.

        Payment payment = null;
        if (order.getPaymentId() != null) {
            payment = paymentRepository.findById(order.getPaymentId()).orElse(null);
        }

        order.setOrderIdentifier(sequenceService.getNextSequence("order_identifier"));
        Order savedOrder = orderRepository.save(order);

        // Update Stock and Sales
        savedOrder.getItems().forEach(item -> {
            productClientService.updateProductStock(item.getId(), item.getQuantity());

            ProductSales productSales = productSalesRepository.findByProductId(item.getId())
                    .orElseGet(() -> {
                        ProductSales ps = new ProductSales();
                        ps.setProductId(item.getId());
                        ps.setSalesCount(0);
                        return ps;
                    });
            int currentSales = productSales.getSalesCount() != null ? productSales.getSalesCount() : 0;
            productSales.setSalesCount(currentSales + item.getQuantity());
            productSalesRepository.save(productSales);
            productSearchEngine.updateSalesCount(productSales.getProductId(), productSales.getSalesCount());
        });

        sendOrderCreatedEmail(savedOrder, carrier, payment);

        // Return full DTO
        return getOrder(savedOrder.getId());
    }

    private void sendOrderCreatedEmail(Order order, Carrier carrier, Payment payment) {
        if (cachedTemplate == null) {
            log.warn("Email template not loaded, skipping email sending.");
            return;
        }

        try {
            String template = cachedTemplate;

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Customer";
            template = template.replace("{{firstname}}", HtmlUtils.htmlEscape(firstname));
            template = template.replace("{{orderIdentifier}}", String.valueOf(order.getOrderIdentifier()));

            // Products
            StringBuilder productsHtml = new StringBuilder();
            BigDecimal carrierPriceVal = BigDecimal.ZERO;
            BigDecimal paymentPriceVal = BigDecimal.ZERO;

            if (order.getPriceBreakDown() != null && order.getPriceBreakDown().getItems() != null) {
                for (PriceItem item : order.getPriceBreakDown().getItems()) {
                    if (item.getType() == PriceItemType.CARRIER) {
                        carrierPriceVal = item.getPriceWithVat();
                        continue;
                    }
                    if (item.getType() == PriceItemType.PAYMENT) {
                        paymentPriceVal = item.getPriceWithVat();
                        continue;
                    }

                    // Show Products and Discounts in the table
                    productsHtml.append("<tr>");
                    productsHtml.append("<td>").append(HtmlUtils.htmlEscape(item.getName())).append("</td>");
                    productsHtml.append("<td>").append(item.getQuantity()).append("</td>");

                    BigDecimal qty = item.getQuantity() != null && item.getQuantity() > 0 ? new BigDecimal(item.getQuantity()) : BigDecimal.ONE;
                    BigDecimal unitPrice = item.getPriceWithVat().divide(qty, 2, RoundingMode.HALF_UP);

                    productsHtml.append("<td>").append(String.format("%.2f €", unitPrice)).append("</td>");
                    productsHtml.append("<td>").append(String.format("%.2f €", item.getPriceWithVat())).append("</td>");
                    productsHtml.append("</tr>");
                }
            }
            template = template.replace("{{products}}", productsHtml.toString());

            // Carrier and Payment
            String carrierName = carrier != null ? carrier.getName() : "Unknown Carrier";
            String carrierPrice = String.format("%.2f €", carrierPriceVal);
            template = template.replace("{{carrierName}}", HtmlUtils.htmlEscape(carrierName));
            template = template.replace("{{carrierPrice}}", carrierPrice);

            String paymentName = payment != null ? payment.getName() : "Unknown Payment";
            String paymentPrice = String.format("%.2f €", paymentPriceVal);
            template = template.replace("{{paymentName}}", HtmlUtils.htmlEscape(paymentName));
            template = template.replace("{{paymentPrice}}", paymentPrice);

            // Address
            StringBuilder addressHtml = new StringBuilder();
            if (order.getDeliveryAddress() != null) {
                addressHtml.append("<p>").append(HtmlUtils.htmlEscape(order.getDeliveryAddress().getStreet())).append("</p>");
                addressHtml.append("<p>").append(HtmlUtils.htmlEscape(order.getDeliveryAddress().getZip())).append(" ")
                           .append(HtmlUtils.htmlEscape(order.getDeliveryAddress().getCity())).append("</p>");
            } else {
                addressHtml.append("<p>No delivery address provided</p>");
            }
            template = template.replace("{{deliveryAddress}}", addressHtml.toString());

            // Final Price
            BigDecimal finalPrice = order.getPriceBreakDown() != null ? order.getPriceBreakDown().getTotalPrice() : BigDecimal.ZERO;
            template = template.replace("{{finalPrice}}", String.format("%.2f €", finalPrice));

            byte[] invoiceBytes = invoiceService.generateInvoice(order.getId());
            File pdfFile = File.createTempFile("faktura_" + order.getOrderIdentifier(), ".pdf");
            try {
                Files.write(pdfFile.toPath(), invoiceBytes);
                emailService.sendEmail(order.getEmail(), "Objednávka č. " + order.getOrderIdentifier(), template, true, pdfFile);
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
//                .filter(order -> order.getCustomerId().equals(getCurrentCustomerId()))
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
                    if (dto.getPriceBreakDown() != null && dto.getPriceBreakDown().getItems() != null) {
                        for (sk.tany.rest.api.dto.PriceItem item : dto.getPriceBreakDown().getItems()) {
                            if (item.getType() == sk.tany.rest.api.dto.PriceItemType.PRODUCT && item.getImage() == null) {
                                if (dto.getItems() != null) {
                                    dto.getItems().stream()
                                            .filter(i -> i.getId().equals(item.getId()))
                                            .findFirst()
                                            .ifPresent(i -> item.setImage(i.getImage()));
                                }
                            }
                        }
                    }
                    return dto;
                })
                .orElseThrow(() -> new OrderException.NotFound("Order not found or access denied"));
    }

    @Override
    public Page<OrderDto> getOrders(String customerId, Pageable pageable) {
        return orderRepository.findAllByCustomerId(customerId, pageable)
                .map(orderMapper::toDto);
    }
}
