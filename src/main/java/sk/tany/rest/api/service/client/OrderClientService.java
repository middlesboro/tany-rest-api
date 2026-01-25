package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.OrderDto;

public interface OrderClientService {
    OrderDto createOrder(OrderDto orderDto);
    OrderDto getOrder(String id);
    Page<OrderDto> getOrders(String customerId, Pageable pageable);
}
