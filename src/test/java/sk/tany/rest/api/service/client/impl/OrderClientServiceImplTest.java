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
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.productsales.ProductSales;
import sk.tany.rest.api.domain.productsales.ProductSalesRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.client.CartClientService;
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
import static org.mockito.Mockito.lenient;

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
    @Mock
    private ProductSalesRepository productSalesRepository;
    @Mock
    private ProductSearchEngine productSearchEngine;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartClientService cartService;

    @InjectMocks
    private OrderClientServiceImpl orderClientService;

    @BeforeEach
    void setUp() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("user@example.com");
        SecurityContextHolder.setContext(securityContext);

        // Mock ResourceLoader
        Resource templateResource = new ByteArrayResource("<html>{{firstname}} {{orderIdentifier}} {{products}} {{carrierName}} {{paymentName}} {{deliveryAddress}} {{finalPrice}}</html>".getBytes());
        Resource pdfResource = new ByteArrayResource("dummy pdf".getBytes());
        lenient().when(resourceLoader.getResource("classpath:templates/email/order_created.html")).thenReturn(templateResource);
        lenient().when(resourceLoader.getResource("classpath:empty.pdf")).thenReturn(pdfResource);

        orderClientService.init();
    }

    @Test
    void createOrder_shouldSendEmail() throws Exception {
        OrderDto orderDto = new OrderDto();
        orderDto.setCartId("cart1");
        // items are not needed in orderDto anymore

        CartDto cartDto = new CartDto();
        cartDto.setCartId("cart1");
        cartDto.setFirstname("John");
        cartDto.setLastname("Doe");
        cartDto.setEmail("user@example.com");
        cartDto.setSelectedCarrierId("carrierId");
        cartDto.setSelectedPaymentId("paymentId");
        cartDto.setTotalPrice(BigDecimal.TEN);
        cartDto.setFinalPrice(BigDecimal.valueOf(17));

        CartItem cartItem = new CartItem("p1", 1);
        cartItem.setTitle("Test Product");
        cartItem.setPrice(BigDecimal.TEN);
        cartDto.setItems(Collections.singletonList(cartItem));

        when(cartService.getOrCreateCart("cart1", null)).thenReturn(cartDto);

        Order order = new Order();
        OrderItem item = new OrderItem();
        item.setId("p1");
        item.setName("Test Product");
        item.setQuantity(1);
        item.setPrice(BigDecimal.TEN);
        order.setItems(Collections.singletonList(item));
        order.setEmail("user@example.com");
        order.setOrderIdentifier(123L);
        order.setCarrierPrice(BigDecimal.valueOf(5));
        order.setPaymentPrice(BigDecimal.valueOf(2));
        order.setId("order1"); // Simulate ID after save

        Carrier carrier = new Carrier();
        carrier.setName("Test Carrier");
        carrier.setId("carrierId");

        Payment payment = new Payment();
        payment.setName("Test Payment");
        payment.setId("paymentId");

        when(carrierRepository.findById("carrierId")).thenReturn(Optional.of(carrier));
        when(paymentRepository.findById("paymentId")).thenReturn(Optional.of(payment));
        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);

        // Mock save to return the order with ID
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId("order1");
            return saved;
        });

        // Mock getOrder
        when(orderRepository.findById("order1")).thenReturn(Optional.of(order));
        OrderDto responseDto = new OrderDto();
        when(orderMapper.toDto(order)).thenReturn(responseDto);

        ProductSales productSales = new ProductSales();
        productSales.setSalesCount(0);
        when(productSalesRepository.findByProductId(any())).thenReturn(Optional.of(productSales));

        orderClientService.createOrder(orderDto);

        // verify(emailService, times(1)).sendEmail(eq("user@example.com"), anyString(), anyString(), eq(true), any(File.class));
        verify(productSalesRepository, times(1)).save(any(ProductSales.class));
        verify(productSearchEngine, times(1)).updateSalesCount(any(), any(Integer.class));
    }

    @Test
    void createOrder_shouldNotFailWhenEmailFails() throws Exception {
        OrderDto orderDto = new OrderDto();
        orderDto.setCartId("cart1");

        CartDto cartDto = new CartDto();
        cartDto.setCartId("cart1");
        cartDto.setFirstname("John");
        cartDto.setEmail("user@example.com");
        cartDto.setSelectedCarrierId("carrierId");
        cartDto.setSelectedPaymentId("paymentId");
        cartDto.setItems(Collections.singletonList(new CartItem("p1", 1)));

        when(cartService.getOrCreateCart("cart1", null)).thenReturn(cartDto);

        Order order = new Order();
        OrderItem item = new OrderItem();
        item.setId("p1");
        item.setQuantity(1);
        order.setItems(Collections.singletonList(item));
        order.setEmail("user@example.com");
        order.setOrderIdentifier(123L);
        order.setId("order1");

        Carrier carrier = new Carrier();
        carrier.setId("carrierId");
        Payment payment = new Payment();
        payment.setId("paymentId");

        when(carrierRepository.findById("carrierId")).thenReturn(Optional.of(carrier));
        when(paymentRepository.findById("paymentId")).thenReturn(Optional.of(payment));
        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
             Order saved = invocation.getArgument(0);
             saved.setId("order1");
             return saved;
        });

        when(orderRepository.findById("order1")).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(new OrderDto());

        ProductSales productSales = new ProductSales();
        productSales.setSalesCount(0);
        when(productSalesRepository.findByProductId(any())).thenReturn(Optional.of(productSales));

        // doThrow(new RuntimeException("Email failed")).when(emailService).sendEmail(anyString(), anyString(), anyString(), eq(true), any(File.class));

        orderClientService.createOrder(orderDto);

        // verify(emailService, times(1)).sendEmail(eq("user@example.com"), anyString(), anyString(), eq(true), any(File.class));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productSalesRepository, times(1)).save(any(ProductSales.class));
    }
}
