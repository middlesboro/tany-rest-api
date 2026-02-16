package sk.tany.rest.api.service.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.customer.Role;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.service.common.EmailService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminNotificationSchedulerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminNotificationScheduler scheduler;

    @Test
    void checkNewOrders_NoOrders_DoesNothing() {
        when(orderRepository.findAllByAdminNotificationDateIsNull()).thenReturn(Collections.emptyList());

        scheduler.checkNewOrders();

        verify(customerRepository, never()).findAllByRole(any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), any());
    }

    @Test
    void checkNewOrders_OrdersFound_NoAdmin_MarksAsNotified() {
        Order order = new Order();
        order.setId("1");
        when(orderRepository.findAllByAdminNotificationDateIsNull()).thenReturn(List.of(order));
        when(customerRepository.findAllByRole(Role.ADMIN)).thenReturn(Collections.emptyList());

        scheduler.checkNewOrders();

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), any());
        verify(orderRepository).save(order);
    }

    @Test
    void checkNewOrders_OrdersFound_AdminFound_SendsEmailAndMarksAsNotified() {
        Order order = new Order();
        order.setId("1");
        order.setOrderIdentifier(123L);
        order.setFinalPrice(BigDecimal.TEN);
        order.setCarrierId("c1");
        order.setPaymentId("p1");
        order.setItems(List.of(new OrderItem("i1", "Product A", 1, BigDecimal.TEN, "img")));

        Customer admin = new Customer();
        admin.setEmail("admin@test.com");

        Carrier carrier = new Carrier();
        carrier.setName("Carrier A");

        Payment payment = new Payment();
        payment.setName("Payment A");

        when(orderRepository.findAllByAdminNotificationDateIsNull()).thenReturn(List.of(order));
        when(customerRepository.findAllByRole(Role.ADMIN)).thenReturn(List.of(admin));
        when(carrierRepository.findById("c1")).thenReturn(Optional.of(carrier));
        when(paymentRepository.findById("p1")).thenReturn(Optional.of(payment));

        scheduler.checkNewOrders();

        verify(emailService).sendEmail(eq("admin@test.com"), anyString(), contains("Product A"), eq(true), isNull());
        verify(orderRepository).save(order);
    }
}
