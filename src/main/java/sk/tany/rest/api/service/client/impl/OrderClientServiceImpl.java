package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.client.OrderClientService;
import sk.tany.rest.api.service.common.SequenceService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderClientServiceImpl implements OrderClientService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerRepository customerRepository;
    private final SequenceService sequenceService;
    private final CarrierRepository carrierRepository;
    private final PaymentRepository paymentRepository;

    private String getCurrentCustomerId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerRepository.findByEmail(email)
                .map(Customer::getId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Override
    public OrderDto createOrder(OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        order.setCustomerId(getCurrentCustomerId());
        order.setOrderIdentifier(sequenceService.getNextSequence("order_identifier"));
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public List<OrderDto> getOrders() {
        return orderRepository.findAllByCustomerId(getCurrentCustomerId())
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrder(String id) {
        return orderRepository.findById(id)
                .filter(order -> order.getCustomerId().equals(getCurrentCustomerId()))
                .map(order -> {
                    OrderDto dto = orderMapper.toDto(order);
                    if (dto.getCarrierId() != null) {
                        carrierRepository.findById(dto.getCarrierId())
                                .ifPresent(carrier -> dto.setCarrierType(carrier.getType()));
                    }
                    if (dto.getPaymentId() != null) {
                        paymentRepository.findById(dto.getPaymentId())
                                .ifPresent(payment -> dto.setPaymentType(payment.getType()));
                    }
                    return dto;
                })
                .orElseThrow(() -> new RuntimeException("Order not found or access denied"));
    }
}
