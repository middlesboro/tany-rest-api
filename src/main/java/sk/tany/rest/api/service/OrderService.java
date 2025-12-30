package sk.tany.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.OrderDto;

import java.util.Optional;

public interface OrderService {
    Page<OrderDto> findAll(Pageable pageable);
    Optional<OrderDto> findById(String id);
    OrderDto save(OrderDto orderDto);
    OrderDto update(String id, OrderDto orderDto);
    void deleteById(String id);
}
