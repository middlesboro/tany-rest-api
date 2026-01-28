package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderAdminServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private SequenceService sequenceService;

    @InjectMocks
    private OrderAdminServiceImpl orderAdminService;

    @Test
    void update_shouldSendEmail_whenStatusChangesToSent() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.SENT);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.SENT);
        updatedOrder.setEmail("test@example.com");
        updatedOrder.setFirstname("John");
        updatedOrder.setOrderIdentifier(100L);
        updatedOrder.setCarrierOrderStateLink("http://track.me");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);

        orderAdminService.update(orderId, orderDto);

        verify(emailService, times(1)).sendEmail(eq("test@example.com"), contains("Order Shipped"), anyString(), eq(true), any());
    }

    @Test
    void update_shouldSendEmail_whenStatusChangesToPaid() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.PAID);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.PAID);
        updatedOrder.setEmail("test@example.com");
        updatedOrder.setFirstname("John");
        updatedOrder.setOrderIdentifier(100L);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);

        orderAdminService.update(orderId, orderDto);

        verify(emailService, times(1)).sendEmail(eq("test@example.com"), contains("Order Paid"), anyString(), eq(true), any());
    }

    @Test
    void update_shouldNotSendEmail_whenStatusDoesNotChangeToSentOrPaid() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.CANCELED);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.CANCELED);
        updatedOrder.setEmail("test@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);

        orderAdminService.update(orderId, orderDto);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), any());
    }

    @Test
    void update_shouldNotSendEmail_whenStatusWasAlreadySent() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.SENT);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.SENT);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.SENT);
        updatedOrder.setEmail("test@example.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);

        orderAdminService.update(orderId, orderDto);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), any());
    }

    @Test
    void update_shouldSetCancelDateAndCreditNoteIdentifier_whenStatusChangesToCanceled() {
        String orderId = "123";
        OrderDto orderDto = new OrderDto();
        orderDto.setStatus(OrderStatus.CANCELED);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.CREATED);

        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setStatus(OrderStatus.CANCELED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toEntity(orderDto)).thenReturn(updatedOrder);
        when(orderRepository.save(updatedOrder)).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(orderDto);
        when(sequenceService.getNextSequence("credit_note_identifier")).thenReturn(555L);

        orderAdminService.update(orderId, orderDto);

        verify(sequenceService, times(1)).getNextSequence("credit_note_identifier");
        assertNotNull(updatedOrder.getCancelDate());
        assertEquals(555L, updatedOrder.getCreditNoteIdentifier());
    }
}
