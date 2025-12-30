package sk.tany.rest.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.mapper.OrderMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById() {
        String id = "1";
        Order order = new Order();
        order.setId(id);
        OrderDto orderDto = new OrderDto();
        orderDto.setId(id);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        Optional<OrderDto> result = orderService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(orderRepository, times(1)).findById(id);
        verify(orderMapper, times(1)).toDto(order);
    }

    @Test
    void save() {
        OrderDto orderDto = new OrderDto();
        Order order = new Order();
        Order savedOrder = new Order();

        when(orderMapper.toEntity(orderDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(orderMapper.toDto(savedOrder)).thenReturn(orderDto);

        OrderDto result = orderService.save(orderDto);

        assertNotNull(result);
        verify(orderRepository, times(1)).save(order);
        verify(orderMapper, times(1)).toEntity(orderDto);
        verify(orderMapper, times(1)).toDto(savedOrder);
    }

    @Test
    void update() {
        String id = "1";
        OrderDto orderDto = new OrderDto();
        Order order = new Order();
        Order savedOrder = new Order();

        when(orderMapper.toEntity(orderDto)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(orderMapper.toDto(savedOrder)).thenReturn(orderDto);

        OrderDto result = orderService.update(id, orderDto);

        assertNotNull(result);
        assertEquals(id, orderDto.getId());
        verify(orderRepository, times(1)).save(order);
        verify(orderMapper, times(1)).toEntity(orderDto);
        verify(orderMapper, times(1)).toDto(savedOrder);
    }

    @Test
    void deleteById() {
        String id = "1";

        doNothing().when(orderRepository).deleteById(id);

        orderService.deleteById(id);

        verify(orderRepository, times(1)).deleteById(id);
    }
}
