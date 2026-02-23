package sk.tany.rest.api.service.client.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
import sk.tany.rest.api.event.OrderStatusChangedEvent;
import sk.tany.rest.api.mapper.OrderMapper;
import sk.tany.rest.api.service.client.CartClientService;
import sk.tany.rest.api.service.client.ProductClientService;
import sk.tany.rest.api.service.common.SequenceService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ProductSalesRepository productSalesRepository;
    @Mock
    private ProductSearchEngine productSearchEngine;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartClientService cartService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderClientServiceImpl orderClientService;

    @BeforeEach
    void setUp() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("user@example.com");
        SecurityContextHolder.setContext(securityContext);
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

        Payment payment = new Payment();
        payment.setName("Test Payment");
        payment.setId("paymentId");

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

        verify(eventPublisher, times(1)).publishEvent(any(OrderStatusChangedEvent.class));
        verify(productSalesRepository, times(1)).save(any(ProductSales.class));
        verify(productSearchEngine, times(1)).updateSalesCount(any(), any(Integer.class));
    }

    @Test
    void createOrder_shouldPublishEvent() {
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

        Payment payment = new Payment();
        payment.setId("paymentId");

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

        orderClientService.createOrder(orderDto);

        verify(eventPublisher, times(1)).publishEvent(any(OrderStatusChangedEvent.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_shouldUpdateCustomerData_whenMissing() {
        OrderDto orderDto = new OrderDto();
        orderDto.setCartId("cart1");

        CartDto cartDto = new CartDto();
        cartDto.setCartId("cart1");
        cartDto.setFirstname("John");
        cartDto.setLastname("Doe");
        cartDto.setPhone("123456789");
        cartDto.setItems(Collections.singletonList(new CartItem("p1", 1)));

        when(cartService.getOrCreateCart("cart1", null)).thenReturn(cartDto);

        Customer customer = new Customer();
        customer.setId("cust1");
        customer.setEmail("user@example.com");
        // Missing firstname, lastname, phone

        when(customerRepository.findByEmail("user@example.com")).thenReturn(Optional.of(customer));
        when(customerRepository.findById("cust1")).thenReturn(Optional.of(customer));

        Order savedOrder = new Order();
        savedOrder.setId("order1");
        savedOrder.setCustomerId("cust1");
        savedOrder.setFirstname("John");
        savedOrder.setLastname("Doe");
        savedOrder.setPhone("123456789");
        savedOrder.setItems(Collections.emptyList());

        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderRepository.findById("order1")).thenReturn(Optional.of(savedOrder));
        when(orderMapper.toDto(savedOrder)).thenReturn(new OrderDto());

        orderClientService.createOrder(orderDto);

        verify(customerRepository).save(customer);
        org.junit.jupiter.api.Assertions.assertEquals("John", customer.getFirstname());
        org.junit.jupiter.api.Assertions.assertEquals("Doe", customer.getLastname());
        org.junit.jupiter.api.Assertions.assertEquals("123456789", customer.getPhone());
    }

    @Test
    void createOrder_shouldNotUpdateCustomerData_whenPresent() {
        OrderDto orderDto = new OrderDto();
        orderDto.setCartId("cart1");

        CartDto cartDto = new CartDto();
        cartDto.setCartId("cart1");
        cartDto.setFirstname("John");
        cartDto.setLastname("Doe");
        cartDto.setPhone("123456789");
        cartDto.setItems(Collections.singletonList(new CartItem("p1", 1)));

        when(cartService.getOrCreateCart("cart1", null)).thenReturn(cartDto);

        Customer customer = new Customer();
        customer.setId("cust1");
        customer.setEmail("user@example.com");
        customer.setFirstname("Existing");
        customer.setLastname("User");
        customer.setPhone("987654321");

        when(customerRepository.findByEmail("user@example.com")).thenReturn(Optional.of(customer));
        when(customerRepository.findById("cust1")).thenReturn(Optional.of(customer));

        Order savedOrder = new Order();
        savedOrder.setId("order1");
        savedOrder.setCustomerId("cust1");
        savedOrder.setFirstname("John");
        savedOrder.setLastname("Doe");
        savedOrder.setPhone("123456789");
        savedOrder.setItems(Collections.emptyList());

        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderRepository.findById("order1")).thenReturn(Optional.of(savedOrder));
        when(orderMapper.toDto(savedOrder)).thenReturn(new OrderDto());

        orderClientService.createOrder(orderDto);

        verify(customerRepository, times(0)).save(customer);
        org.junit.jupiter.api.Assertions.assertEquals("Existing", customer.getFirstname());
        org.junit.jupiter.api.Assertions.assertEquals("User", customer.getLastname());
        org.junit.jupiter.api.Assertions.assertEquals("987654321", customer.getPhone());
    }

    @Test
    void createOrder_shouldSetAuthenticatedUser() {
        OrderDto orderDto = new OrderDto();
        orderDto.setCartId("cart1");

        CartDto cartDto = new CartDto();
        cartDto.setCartId("cart1");
        cartDto.setItems(Collections.singletonList(new CartItem("p1", 1)));

        when(cartService.getOrCreateCart("cart1", null)).thenReturn(cartDto);

        // Mock authenticated user
        Customer customer = new Customer();
        customer.setId("cust1");
        when(customerRepository.findByEmail("user@example.com")).thenReturn(Optional.of(customer));

        Order savedOrder = new Order();
        savedOrder.setId("order1");
        savedOrder.setCustomerId("cust1");
        savedOrder.setAuthenticatedUser(true);
        savedOrder.setItems(Collections.emptyList());

        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("order1");
            return o;
        });
        when(orderRepository.findById("order1")).thenReturn(Optional.of(savedOrder));
        when(orderMapper.toDto(savedOrder)).thenReturn(new OrderDto());

        orderClientService.createOrder(orderDto);

        verify(orderRepository).save(org.mockito.ArgumentMatchers.argThat(Order::isAuthenticatedUser));
    }

    @Test
    void createOrder_shouldSetAuthenticatedUserFalse_whenNotLoggedIn() {
        // Reset security context or return null email/customer
        when(customerRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        OrderDto orderDto = new OrderDto();
        orderDto.setCartId("cart1");

        CartDto cartDto = new CartDto();
        cartDto.setCartId("cart1");
        cartDto.setItems(Collections.singletonList(new CartItem("p1", 1)));

        when(cartService.getOrCreateCart("cart1", null)).thenReturn(cartDto);

        Order savedOrder = new Order();
        savedOrder.setId("order1");
        savedOrder.setAuthenticatedUser(false);
        savedOrder.setItems(Collections.emptyList());

        when(sequenceService.getNextSequence("order_identifier")).thenReturn(123L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("order1");
            return o;
        });
        when(orderRepository.findById("order1")).thenReturn(Optional.of(savedOrder));
        when(orderMapper.toDto(savedOrder)).thenReturn(new OrderDto());

        orderClientService.createOrder(orderDto);

        verify(orderRepository).save(org.mockito.ArgumentMatchers.argThat(o -> !o.isAuthenticatedUser()));
    }
}
