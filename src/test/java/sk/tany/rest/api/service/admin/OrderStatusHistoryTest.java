package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.mapper.OrderMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusHistoryTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderAdminServiceImpl orderAdminService;

    private Order order;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId("1");
        order.setStatus(OrderStatus.CREATED);
        order.setStatusHistory(new ArrayList<>());
        order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.CREATED, Instant.now()));

        orderDto = new OrderDto();
        orderDto.setId("1");
    }

    @Test
    void update_shouldAddHistory_whenStatusChanges() {
        // Arrange
        orderDto.setStatus(OrderStatus.PAID);

        Order updatedOrder = new Order();
        updatedOrder.setId("1");
        updatedOrder.setStatus(OrderStatus.PAID);

        when(orderRepository.findById("1")).thenReturn(Optional.of(order));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        // Act
        orderAdminService.update("1", orderDto);

        // Assert
        verify(orderRepository).save(any(Order.class));
        assertEquals(2, updatedOrder.getStatusHistory().size());
        assertEquals(OrderStatus.PAID, updatedOrder.getStatusHistory().get(1).getStatus());
    }

    @Test
    void update_shouldNotAddHistory_whenStatusDoesNotChange() {
        // Arrange
        orderDto.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId("1");
        updatedOrder.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById("1")).thenReturn(Optional.of(order));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        // Act
        orderAdminService.update("1", orderDto);

        // Assert
        verify(orderRepository).save(any(Order.class));
        // Should only have existing history (1 record copied from old order)
        assertEquals(1, updatedOrder.getStatusHistory().size());
        assertEquals(OrderStatus.CREATED, updatedOrder.getStatusHistory().get(0).getStatus());
    }

    @Test
    void save_shouldInitHistory_whenNewOrder() {
        Order newOrder = new Order(); // ID null, status CREATED default
        OrderDto newDto = new OrderDto();

        when(orderMapper.toEntity(newDto)).thenReturn(newOrder);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(newDto);

        orderAdminService.save(newDto);

        assertNotNull(newOrder.getStatusHistory());
        assertEquals(1, newOrder.getStatusHistory().size());
        assertEquals(OrderStatus.CREATED, newOrder.getStatusHistory().get(0).getStatus());
    }
}
