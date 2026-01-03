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
import sk.tany.rest.api.controller.admin.OrderAdminController;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.admin.order.list.OrderAdminListResponse;
import sk.tany.rest.api.mapper.OrderAdminApiMapper;
import sk.tany.rest.api.service.admin.OrderAdminService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderAdminControllerTest {

    @Mock
    private OrderAdminService orderService;

    @Mock
    private OrderAdminApiMapper orderAdminApiMapper;

    @InjectMocks
    private OrderAdminController orderAdminController;

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

        OrderAdminListResponse response = new OrderAdminListResponse();
        response.setId("123");

        when(orderService.findAll(pageable)).thenReturn(orderPage);
        when(orderAdminApiMapper.toListResponse(orderDto)).thenReturn(response);

        Page<OrderAdminListResponse> result = orderAdminController.getOrders(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("123", result.getContent().get(0).getId());
        verify(orderService, times(1)).findAll(pageable);
    }
}
