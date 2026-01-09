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
        order.setSelectedPickupPointId(orderDto.getSelectedPickupPointId());
        try {
            order.setCustomerId(getCurrentCustomerId());
        } catch (Exception e) {
            // nothing to do. if customer not found, order will be created without customerId
        }
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
