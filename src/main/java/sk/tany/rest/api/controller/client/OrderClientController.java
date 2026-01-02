package sk.tany.rest.api.controller.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.service.client.OrderClientService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order API")
public class OrderClientController {

    private final OrderClientService orderClientService;

    @Operation(summary = "Create order")
    @PostMapping
    public OrderDto createOrder(@RequestBody OrderDto orderDto) {
        return orderClientService.createOrder(orderDto);
    }

    @Operation(summary = "Get current user's orders")
    @GetMapping
    public List<OrderDto> getOrders() {
        return orderClientService.getOrders();
    }

    @Operation(summary = "Get order details")
    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable String id) {
        return orderClientService.getOrder(id);
    }
}
