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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.tany.rest.api.controller.admin.OrderAdminController;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PriceBreakDown;
import sk.tany.rest.api.dto.admin.order.get.OrderAdminGetResponse;
import sk.tany.rest.api.dto.admin.order.list.OrderAdminListResponse;
import sk.tany.rest.api.mapper.OrderAdminApiMapper;
import sk.tany.rest.api.service.admin.InvoiceService;
import sk.tany.rest.api.service.admin.OrderAdminService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class OrderAdminControllerTest {

    @Mock
    private OrderAdminService orderService;

    @Mock
    private InvoiceService invoiceService;

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

        when(orderService.findAll(null, null, null, null, null, null, null, null, pageable)).thenReturn(orderPage);
        when(orderAdminApiMapper.toListResponse(orderDto)).thenReturn(response);

        Page<OrderAdminListResponse> result = orderAdminController.getOrders(null, null, null, null, null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("123", result.getContent().getFirst().getId());
        verify(orderService, times(1)).findAll(null, null, null, null, null, null, null, null, pageable);
    }

    @Test
    void getOrder_ShouldReturnOrderWithPriceBreakDown() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);
        PriceBreakDown priceBreakDown = new PriceBreakDown();
        priceBreakDown.setTotalPrice(BigDecimal.TEN);
        orderDto.setPriceBreakDown(priceBreakDown);

        OrderAdminGetResponse response = new OrderAdminGetResponse();
        response.setId(orderId);
        response.setPriceBreakDown(priceBreakDown);

        when(orderService.findById(orderId)).thenReturn(Optional.of(orderDto));
        when(orderAdminApiMapper.toGetResponse(orderDto)).thenReturn(response);

        ResponseEntity<OrderAdminGetResponse> result = orderAdminController.getOrder(orderId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(orderId, result.getBody().getId());
        assertEquals(priceBreakDown, result.getBody().getPriceBreakDown());
        verify(orderService).findById(orderId);
        verify(orderAdminApiMapper).toGetResponse(orderDto);
    }

    @Test
    void getOrder_ShouldReturnNotFound() {
        String orderId = "123";
        when(orderService.findById(orderId)).thenReturn(Optional.empty());

        ResponseEntity<OrderAdminGetResponse> result = orderAdminController.getOrder(orderId);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(orderService).findById(orderId);
    }

    @Test
    void getOrderInvoice_ShouldReturnPdf() {
        String orderId = "123";
        byte[] pdfContent = "PDF".getBytes();
        OrderDto order = new OrderDto();
        order.setOrderIdentifier(1L);

        when(orderService.findById(orderId)).thenReturn(Optional.of(order));
        when(invoiceService.generateInvoice(orderId)).thenReturn(pdfContent);

        ResponseEntity<byte[]> response = orderAdminController.getOrderInvoice(orderId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(pdfContent, response.getBody());
        verify(invoiceService).generateInvoice(orderId);
    }
}
