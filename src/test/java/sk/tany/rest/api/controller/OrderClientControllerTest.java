package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.controller.client.OrderClientController;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.service.client.OrderClientService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderClientControllerTest {

    @Mock
    private OrderClientService orderService;

    @InjectMocks
    private OrderClientController orderClientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        OrderDto orderDto = new OrderDto();
        orderDto.setId("1");

        when(orderService.createOrder(any(OrderDto.class))).thenReturn(orderDto);

        OrderDto result = orderClientController.createOrder(orderDto);

        assertEquals("1", result.getId());
        verify(orderService, times(1)).createOrder(orderDto);
    }

    @Test
    void getOrders_ShouldReturnListOfOrders() {
        OrderDto orderDto = new OrderDto();
        orderDto.setId("1");
        List<OrderDto> orders = Collections.singletonList(orderDto);

        when(orderService.getOrders()).thenReturn(orders);

        List<OrderDto> result = orderClientController.getOrders();

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        verify(orderService, times(1)).getOrders();
    }

    @Test
    void getOrder_ShouldReturnOrder() {
        OrderDto orderDto = new OrderDto();
        orderDto.setId("1");

        when(orderService.getOrder("1")).thenReturn(orderDto);

        OrderDto result = orderClientController.getOrder("1");

        assertEquals("1", result.getId());
        verify(orderService, times(1)).getOrder("1");
    }
}
