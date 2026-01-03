package sk.tany.rest.api.controller.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateRequest;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateResponse;
import sk.tany.rest.api.dto.client.order.get.OrderClientGetResponse;
import sk.tany.rest.api.dto.client.order.list.OrderClientListResponse;
import sk.tany.rest.api.mapper.OrderClientApiMapper;
import sk.tany.rest.api.service.client.OrderClientService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order API")
public class OrderClientController {

    private final OrderClientService orderClientService;
    private final OrderClientApiMapper orderClientApiMapper;

    @Operation(summary = "Create order")
    @PostMapping
    public OrderClientCreateResponse createOrder(@RequestBody OrderClientCreateRequest orderDto) {
        OrderDto dto = orderClientApiMapper.toDto(orderDto);
        OrderDto createdOrder = orderClientService.createOrder(dto);
        return orderClientApiMapper.toCreateResponse(createdOrder);
    }

    @Operation(summary = "Get current user's orders")
    @GetMapping
    public List<OrderClientListResponse> getOrders() {
        return orderClientService.getOrders().stream()
                .map(orderClientApiMapper::toListResponse)
                .toList();
    }

    @Operation(summary = "Get order details")
    @GetMapping("/{id}")
    public OrderClientGetResponse getOrder(@PathVariable String id) {
        OrderDto order = orderClientService.getOrder(id);
        return orderClientApiMapper.toGetResponse(order);
    }
}
