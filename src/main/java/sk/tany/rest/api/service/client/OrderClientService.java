package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.OrderDto;
import java.util.List;

public interface OrderClientService {
    OrderDto createOrder(OrderDto orderDto);
    List<OrderDto> getOrders();
    OrderDto getOrder(String id);
}
