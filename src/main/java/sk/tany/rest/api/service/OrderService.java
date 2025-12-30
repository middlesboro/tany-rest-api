package sk.tany.rest.api.service;

import sk.tany.rest.api.dto.OrderDto;

import java.util.Optional;

public interface OrderService {
    Optional<OrderDto> findById(String id);
    OrderDto save(OrderDto orderDto);
    OrderDto update(String id, OrderDto orderDto);
    void deleteById(String id);
}
