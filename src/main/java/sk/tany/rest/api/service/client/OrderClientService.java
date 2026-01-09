package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.OrderDto;

public interface OrderClientService {
    OrderDto createOrder(OrderDto orderDto);
    OrderDto getOrder(String id);
}
