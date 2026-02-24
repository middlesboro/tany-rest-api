package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.carrier.CarrierType;
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
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.event.OrderStatusChangedEvent;
import sk.tany.rest.api.exception.OrderException;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.client.OrderClientService;
import sk.tany.rest.api.service.client.ProductClientService;
import sk.tany.rest.api.service.common.SequenceService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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
    private final ProductSalesRepository productSalesRepository;
    private final ProductSearchEngine productSearchEngine;
    private final CartRepository cartRepository;
    private final sk.tany.rest.api.service.client.CartClientService cartService;
    private final ApplicationEventPublisher eventPublisher;

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
        return new Address(dto.getStreet(), dto.getCity(), dto.getZip(), dto.getCountry());
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
        order.setAuthenticatedUser(order.getCustomerId() != null);

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
                    .toList();
            order.setAppliedDiscountCodes(codes);
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
            if (payment != null && PaymentType.COD == payment.getType()) {
                order.setStatus(OrderStatus.COD);
                order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.COD, Instant.now()));
            }
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

        eventPublisher.publishEvent(new OrderStatusChangedEvent(savedOrder));

        updateCustomerFromOrder(savedOrder);

        // Return full DTO
        return getOrder(savedOrder.getId());
    }

    private void updateCustomerFromOrder(Order order) {
        if (order.getCustomerId() == null) {
            return;
        }
        customerRepository.findById(order.getCustomerId()).ifPresent(customer -> {
            boolean changed = false;
            if (StringUtils.isBlank(customer.getFirstname()) && StringUtils.isNotBlank(order.getFirstname())) {
                customer.setFirstname(order.getFirstname());
                changed = true;
            }
            if (StringUtils.isBlank(customer.getLastname()) && StringUtils.isNotBlank(order.getLastname())) {
                customer.setLastname(order.getLastname());
                changed = true;
            }
            if (StringUtils.isBlank(customer.getPhone()) && StringUtils.isNotBlank(order.getPhone())) {
                customer.setPhone(order.getPhone());
                changed = true;
            }
            if (customer.getInvoiceAddress() == null && order.getInvoiceAddress() != null) {
                customer.setInvoiceAddress(new Address());
            }
            if (updateCustomerAddressFromOrderAddress(customer.getInvoiceAddress(), order.getInvoiceAddress())) {
                changed = true;
            }
            if (customer.getDeliveryAddress() == null && order.getDeliveryAddress() != null) {
                customer.setDeliveryAddress(new Address());
            }
            if (updateCustomerAddressFromOrderAddress(customer.getDeliveryAddress(), order.getDeliveryAddress())) {
                changed = true;
            }

            Optional<Carrier> carrierOptional = carrierRepository.findById(order.getCarrierId());
            if (carrierOptional.isPresent()) {
                Carrier carrier = carrierOptional.get();
                if (StringUtils.isBlank(customer.getPreferredPacketaBranchId()) &&  StringUtils.isNotBlank(order.getSelectedPickupPointId())
                        && CarrierType.PACKETA == carrier.getType()) {
                    customer.setPreferredPacketaBranchId(order.getSelectedPickupPointId());
                    changed = true;
                }
                if (StringUtils.isBlank(customer.getPreferredBalikovoBranchId()) &&  StringUtils.isNotBlank(order.getSelectedPickupPointId())
                        && CarrierType.BALIKOVO == carrier.getType()) {
                    customer.setPreferredBalikovoBranchId(order.getSelectedPickupPointId());
                    changed = true;
                }
            }
            if (changed) {
                customerRepository.save(customer);
            }
        });
    }

    private boolean updateCustomerAddressFromOrderAddress(Address customerAddress, Address orderAddress) {
        if (customerAddress == null || orderAddress == null) {
            return false;
        }
        boolean changed = false;
        if (StringUtils.isBlank(customerAddress.getCity()) && StringUtils.isNotBlank(orderAddress.getCity())) {
            customerAddress.setCity(orderAddress.getCity());
            changed = true;
        }
        if (StringUtils.isBlank(customerAddress.getStreet()) && StringUtils.isNotBlank(orderAddress.getStreet())) {
            customerAddress.setStreet(orderAddress.getStreet());
            changed = true;
        }
        if (StringUtils.isBlank(customerAddress.getZip()) && StringUtils.isNotBlank(orderAddress.getZip())) {
            customerAddress.setZip(orderAddress.getZip());
            changed = true;
        }

        return changed;
    }

    @Override
    public OrderDto getOrder(String id) {
        return orderRepository.findById(id)
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
        return orderRepository.findAllByCustomerIdAndAuthenticatedUserTrue(customerId, pageable)
                .map(orderMapper::toDto);
    }
}
