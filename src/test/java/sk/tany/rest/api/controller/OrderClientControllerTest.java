package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.controller.client.OrderClientController;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateRequest;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateResponse;
import sk.tany.rest.api.dto.client.order.get.OrderClientGetResponse;
import sk.tany.rest.api.mapper.OrderClientApiMapper;
import sk.tany.rest.api.service.client.OrderClientService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderClientControllerTest {

    @Mock
    private OrderClientService orderService;

    @Mock
    private OrderClientApiMapper orderClientApiMapper;

    @InjectMocks
    private OrderClientController orderClientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        OrderClientCreateRequest request = new OrderClientCreateRequest();
        OrderDto orderDto = new OrderDto();
        orderDto.setId("1");
        OrderClientCreateResponse response = new OrderClientCreateResponse();
        response.setId("1");

        when(orderClientApiMapper.toDto(request)).thenReturn(orderDto);
        when(orderService.createOrder(orderDto)).thenReturn(orderDto);
        when(orderClientApiMapper.toCreateResponse(orderDto)).thenReturn(response);

        OrderClientCreateResponse result = orderClientController.createOrder(request);

        assertEquals("1", result.getId());
        verify(orderService, times(1)).createOrder(orderDto);
    }

    @Test
    void getOrder_ShouldReturnOrder() {
        OrderDto orderDto = new OrderDto();
        orderDto.setId("1");
        OrderClientGetResponse response = new OrderClientGetResponse();
        response.setId("1");

        when(orderService.getOrder("1")).thenReturn(orderDto);
        when(orderClientApiMapper.toGetResponse(orderDto)).thenReturn(response);

        OrderClientGetResponse result = orderClientController.getOrder("1");

        assertEquals("1", result.getId());
        verify(orderService, times(1)).getOrder("1");
    }
}
