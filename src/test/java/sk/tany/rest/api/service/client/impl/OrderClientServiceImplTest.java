package sk.tany.rest.api.service.client.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.client.ProductClientService;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class OrderClientServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private SequenceService sequenceService;
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ProductClientService productClientService;
    @Mock
    private EmailService emailService;
    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private OrderClientServiceImpl orderClientService;

    @BeforeEach
    void setUp() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createOrder_shouldSendEmail() throws Exception {
        OrderDto orderDto = new OrderDto();
        orderDto.setItems(Collections.singletonList(new OrderItemDto()));
        orderDto.setCarrierId("carrierId");
        orderDto.setPaymentId("paymentId");

        Order order = new Order();
        order.setItems(Collections.singletonList(new OrderItem()));
        order.setEmail("user@example.com");
        order.setOrderIdentifier(123L);

        when(orderMapper.toEntity(orderDto)).thenReturn(order);
        when(customerRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new Customer()));
        when(productClientService.findAllByIds(any())).thenReturn(Collections.emptyList());
        when(carrierRepository.findById("carrierId")).thenReturn(Optional.of(new Carrier()));
        when(paymentRepository.findById("paymentId")).thenReturn(Optional.of(new Payment()));
        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        // Mock ResourceLoader
        Resource templateResource = new ByteArrayResource("<html>{{firstname}} {{orderIdentifier}}</html>".getBytes());
        Resource pdfResource = new ByteArrayResource("dummy pdf".getBytes());
        when(resourceLoader.getResource("classpath:templates/email/order_created.html")).thenReturn(templateResource);
        when(resourceLoader.getResource("classpath:empty.pdf")).thenReturn(pdfResource);

        orderClientService.createOrder(orderDto);

        verify(emailService, times(1)).sendEmail(eq("user@example.com"), anyString(), anyString(), eq(true), any(File.class));
    }

    @Test
    void createOrder_shouldNotFailWhenEmailFails() throws Exception {
        OrderDto orderDto = new OrderDto();
        orderDto.setItems(Collections.singletonList(new OrderItemDto()));
        orderDto.setCarrierId("carrierId");
        orderDto.setPaymentId("paymentId");

        Order order = new Order();
        order.setItems(Collections.singletonList(new OrderItem()));
        order.setEmail("user@example.com");
        order.setOrderIdentifier(123L);

        when(orderMapper.toEntity(orderDto)).thenReturn(order);
        when(customerRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new Customer()));
        when(productClientService.findAllByIds(any())).thenReturn(Collections.emptyList());
        when(carrierRepository.findById("carrierId")).thenReturn(Optional.of(new Carrier()));
        when(paymentRepository.findById("paymentId")).thenReturn(Optional.of(new Payment()));
        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        // Mock ResourceLoader
        Resource templateResource = new ByteArrayResource("<html>{{firstname}} {{orderIdentifier}}</html>".getBytes());
        Resource pdfResource = new ByteArrayResource("dummy pdf".getBytes());
        when(resourceLoader.getResource("classpath:templates/email/order_created.html")).thenReturn(templateResource);
        when(resourceLoader.getResource("classpath:empty.pdf")).thenReturn(pdfResource);

        doThrow(new RuntimeException("Email failed")).when(emailService).sendEmail(anyString(), anyString(), anyString(), eq(true), any(File.class));

        orderClientService.createOrder(orderDto);

        verify(emailService, times(1)).sendEmail(eq("user@example.com"), anyString(), anyString(), eq(true), any(File.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
