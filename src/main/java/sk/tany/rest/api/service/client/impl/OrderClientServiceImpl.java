package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.dto.CarrierDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.helper.OrderHelper;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.client.CarrierClientService;
import sk.tany.rest.api.service.client.CustomerClientService;
import sk.tany.rest.api.service.client.OrderClientService;
import sk.tany.rest.api.service.client.PaymentClientService;
import sk.tany.rest.api.service.client.ProductClientService;
import sk.tany.rest.api.service.common.SequenceService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderClientServiceImpl implements OrderClientService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerClientService customerClientService;
    private final SequenceService sequenceService;
    private final CarrierClientService carrierClientService;
    private final PaymentClientService paymentClientService;
    private final ProductClientService productClientService;

    private String getCurrentCustomerId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomerDto customer = customerClientService.findByEmail(email);
        if (customer != null) {
            return customer.getId();
        }
        throw new RuntimeException("Customer not found");
    }

    @Override
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

        Optional<CarrierDto> carrierOptional = carrierClientService.findById(orderDto.getCarrierId());
        if (carrierOptional.isPresent()) {
            CarrierDto carrier = carrierOptional.get();

            BigDecimal totalWeight = OrderHelper.getProductsWeight(products);
            order.setCarrierPrice(OrderHelper.getCarrierPrice(carrier, totalWeight));
        }

        Optional<PaymentDto> paymentOptional = paymentClientService.findById(orderDto.getPaymentId());
        if (paymentOptional.isPresent()) {
            PaymentDto payment = paymentOptional.get();
            order.setPaymentPrice(payment.getPrice());
        }

        // add carrier price and payment price
        order.setOrderIdentifier(sequenceService.getNextSequence("order_identifier"));
        return orderMapper.toDto(orderRepository.save(order));
    }

    // todo do it e.g. accessible for 1h after creation. otherwise needed authorization. or find better solution
    @Override
    public OrderDto getOrder(String id) {
        return orderRepository.findById(id)
                .filter(order -> order.getCustomerId().equals(getCurrentCustomerId()))
                .map(order -> {
                    OrderDto dto = orderMapper.toDto(order);
                    if (dto.getCarrierId() != null) {
                        carrierClientService.findById(dto.getCarrierId())
                                .ifPresent(carrier -> {
                                    dto.setCarrierType(carrier.getType());
                                    dto.setCarrierName(carrier.getName());
                                });
                    }
                    if (dto.getPaymentId() != null) {
                        paymentClientService.findById(dto.getPaymentId())
                                .ifPresent(payment -> {
                                    dto.setPaymentType(payment.getType());
                                    dto.setPaymentName(payment.getName());
                                });
                    }
                    return dto;
                })
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));
    }

    @Override
    public void updateStatus(String orderId, sk.tany.rest.api.domain.order.OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }
}
