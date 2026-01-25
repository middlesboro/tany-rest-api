package sk.tany.rest.api.controller.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.component.SecurityUtil;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateRequest;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateResponse;
import sk.tany.rest.api.dto.client.order.get.OrderClientGetResponse;
import sk.tany.rest.api.dto.client.order.list.OrderClientListResponse;
import sk.tany.rest.api.mapper.OrderClientApiMapper;
import sk.tany.rest.api.service.client.OrderClientService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order API")
public class OrderClientController {

    private final OrderClientService orderClientService;
    private final OrderClientApiMapper orderClientApiMapper;
    private final SecurityUtil securityUtil;

    @Operation(summary = "Create order")
    @PostMapping
    public OrderClientCreateResponse createOrder(@RequestBody OrderClientCreateRequest orderDto) {
        OrderDto dto = orderClientApiMapper.toDto(orderDto);
        OrderDto createdOrder = orderClientService.createOrder(dto);
        return orderClientApiMapper.toCreateResponse(createdOrder);
    }

    // todo this have to be under authentication
    @Operation(summary = "Get order details")
    @GetMapping("/{id}")
    public OrderClientGetResponse getOrder(@PathVariable String id) {
        OrderDto order = orderClientService.getOrder(id);
        return orderClientApiMapper.toGetResponse(order);
    }

    @Operation(summary = "Get all orders")
    @GetMapping
    public Page<OrderClientListResponse> getOrders(Pageable pageable) {
        String customerId = securityUtil.getLoggedInUserId();
        Page<OrderDto> orders = orderClientService.getOrders(customerId, pageable);
        return orders.map(orderClientApiMapper::toListResponse);
    }
}
