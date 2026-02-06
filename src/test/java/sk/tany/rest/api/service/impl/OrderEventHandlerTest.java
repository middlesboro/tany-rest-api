package sk.tany.rest.api.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.event.OrderStatusChangedEvent;
import sk.tany.rest.api.service.admin.InvoiceService;
import sk.tany.rest.api.service.common.EmailService;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @BeforeEach
    void setUp() {
        // Mock ResourceLoader for template and files
        Resource templateResource = new ByteArrayResource("<html>{{firstname}} {{orderIdentifier}}</html>".getBytes());
        Resource pdfResource = new ByteArrayResource("dummy pdf".getBytes());

        // Note: ClassPathResource used in getEmailTemplate works differently than ResourceLoader
        // But OrderEventHandler uses getEmailTemplate("templates/email/order_created.html")
        // which creates new ClassPathResource("templates/email/order_created.html").
        // This is hard to mock unless we use PowerMock or modify the class to use ResourceLoader for templates too.
        // Wait, OrderEventHandler uses `new ClassPathResource(template)`. This reads from classpath.
        // If the file exists in src/main/resources, it will read it.
        // If not, it will fail.
        // The file `templates/email/order_created.html` exists in the repo, so it should be fine in integration tests,
        // but unit tests might not find it if it's not on classpath during test run?
        // Usually src/main/resources is on classpath during tests.

        // However, for attachments, it uses resourceLoader.getResource(...).
        lenient().when(resourceLoader.getResource("classpath:formular-na-odstupenie-od-zmluvy-tany.sk.pdf")).thenReturn(pdfResource);
        lenient().when(resourceLoader.getResource("classpath:obchodne-podmienky.pdf")).thenReturn(pdfResource);

        lenient().when(invoiceService.generateInvoice(anyString())).thenReturn(new byte[0]);
    }

    @Test
    void handleOrderStatusChanged_shouldSendEmail_whenCreated() {
        Order order = new Order();
        order.setId("order1");
        order.setOrderIdentifier(123L);
        order.setFirstname("John");
        order.setEmail("user@example.com");
        order.setStatus(OrderStatus.CREATED);
        order.setCarrierId("carrier1");
        order.setPaymentId("payment1");
        order.setStatusHistory(new ArrayList<>());
        order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.CREATED, Instant.now()));

        when(carrierRepository.findById("carrier1")).thenReturn(Optional.empty());
        when(paymentRepository.findById("payment1")).thenReturn(Optional.empty());

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(order);
        orderEventHandler.handleOrderStatusChanged(event);

        verify(emailService, times(1)).sendEmail(eq("user@example.com"), anyString(), anyString(), eq(true), any(File.class), any(File.class), any(File.class));
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void handleOrderStatusChanged_shouldSendEmail_whenCOD() {
        Order order = new Order();
        order.setId("order1");
        order.setOrderIdentifier(123L);
        order.setFirstname("John");
        order.setEmail("user@example.com");
        order.setStatus(OrderStatus.COD);
        order.setCarrierId("carrier1");
        order.setPaymentId("payment1");
        order.setStatusHistory(new ArrayList<>());
        order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.CREATED, Instant.now()));
        order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.COD, Instant.now()));

        when(carrierRepository.findById("carrier1")).thenReturn(Optional.empty());
        when(paymentRepository.findById("payment1")).thenReturn(Optional.empty());

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(order);
        orderEventHandler.handleOrderStatusChanged(event);

        verify(emailService, times(1)).sendEmail(eq("user@example.com"), anyString(), anyString(), eq(true), any(File.class), any(File.class), any(File.class));
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void handleOrderStatusChanged_shouldNotSendEmail_whenAlreadySent() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setStatusHistory(new ArrayList<>());
        OrderStatusHistory history = new OrderStatusHistory(OrderStatus.CREATED, Instant.now());
        history.setEmailSent(true);
        order.getStatusHistory().add(history);

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(order);
        orderEventHandler.handleOrderStatusChanged(event);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), any(Boolean.class), any(File.class));
    }
}
