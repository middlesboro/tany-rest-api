package sk.tany.rest.api.service.chat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderToolsTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderTools orderTools;

    @Test
    void checkOrderStatus_invalidId() {
        String result = orderTools.checkOrderStatus("abc", "email@test.com");
        assertEquals("Invalid order identifier format. It must be a number.", result);
    }

    @Test
    void checkOrderStatus_notFound() {
        when(orderRepository.findByOrderIdentifier(123L)).thenReturn(Optional.empty());
        String result = orderTools.checkOrderStatus("123", "email@test.com");
        assertEquals("Order with identifier 123 not found.", result);
    }

    @Test
    void checkOrderStatus_matchEmail() {
        Order order = new Order();
        order.setOrderIdentifier(123L);
        order.setEmail("test@example.com");
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findByOrderIdentifier(123L)).thenReturn(Optional.of(order));

        String result = orderTools.checkOrderStatus("123", "test@example.com");
        assertTrue(result.contains("CREATED"));
    }

    @Test
    void checkOrderStatus_matchPhone_sameFormat() {
        Order order = new Order();
        order.setOrderIdentifier(123L);
        order.setPhone("0948123456");
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findByOrderIdentifier(123L)).thenReturn(Optional.of(order));

        String result = orderTools.checkOrderStatus("123", "0948123456");
        assertTrue(result.contains("PAID"));
    }

    @Test
    void checkOrderStatus_matchPhone_inputPlus421() {
        Order order = new Order();
        order.setOrderIdentifier(123L);
        order.setPhone("0948123456");
        order.setStatus(OrderStatus.SENT);

        when(orderRepository.findByOrderIdentifier(123L)).thenReturn(Optional.of(order));

        String result = orderTools.checkOrderStatus("123", "+421 948 123 456");
        assertTrue(result.contains("SENT"));
    }

    @Test
    void checkOrderStatus_matchPhone_storedPlus421() {
        Order order = new Order();
        order.setOrderIdentifier(123L);
        order.setPhone("+421 948 123 456");
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findByOrderIdentifier(123L)).thenReturn(Optional.of(order));

        String result = orderTools.checkOrderStatus("123", "0948123456");
        assertTrue(result.contains("DELIVERED"));
    }

    @Test
    void checkOrderStatus_mismatch() {
        Order order = new Order();
        order.setOrderIdentifier(123L);
        order.setEmail("other@example.com");
        order.setPhone("0900000000");
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findByOrderIdentifier(123L)).thenReturn(Optional.of(order));

        String result = orderTools.checkOrderStatus("123", "wrong@example.com");
        assertEquals("Order found, but the provided email or phone number does not match our records.", result);
    }
}
