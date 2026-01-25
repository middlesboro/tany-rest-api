package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.mapper.OrderMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderAdminServiceImpl implements OrderAdminService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Page<OrderDto> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toDto);
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
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public void deleteById(String id) {
        orderRepository.deleteById(id);
    }
}
