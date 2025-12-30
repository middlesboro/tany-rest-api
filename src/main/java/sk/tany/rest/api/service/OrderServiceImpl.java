package sk.tany.rest.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.mapper.OrderMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

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
        var savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderDto update(String id, OrderDto orderDto) {
        orderDto.setId(id);
        var order = orderMapper.toEntity(orderDto);
        var savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public void deleteById(String id) {
        orderRepository.deleteById(id);
    }
}
