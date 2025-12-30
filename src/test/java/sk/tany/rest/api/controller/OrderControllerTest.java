package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.service.OrderService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOrders_ShouldReturnPagedOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        OrderDto orderDto = new OrderDto();
        orderDto.setId("123");
        Page<OrderDto> orderPage = new PageImpl<>(Collections.singletonList(orderDto));

        when(orderService.findAll(pageable)).thenReturn(orderPage);

        Page<OrderDto> result = orderController.getOrders(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("123", result.getContent().get(0).getId());
        verify(orderService, times(1)).findAll(pageable);
    }
}
