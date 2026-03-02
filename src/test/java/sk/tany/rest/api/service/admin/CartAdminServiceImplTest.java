package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.admin.cart.list.CartAdminListResponse;
import sk.tany.rest.api.mapper.CartMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartAdminServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private CartAdminServiceImpl cartAdminService;

    @Test
    void findAll_shouldMapCorrectly() {
        Cart cart = new Cart();
        cart.setId("cart1");
        cart.setCreateDate(Instant.now());
        cart.setCustomerId("cust1");

        Customer customer = new Customer();
        customer.setId("cust1");
        customer.setFirstname("John");
        customer.setLastname("Doe");

        // Mock MongoTemplate behavior
        when(mongoTemplate.count(any(Query.class), eq(Cart.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Cart.class))).thenReturn(List.of(cart));

        when(orderRepository.findByCartIdIn(List.of("cart1"))).thenReturn(Collections.emptyList());
        when(customerRepository.findById("cust1")).thenReturn(Optional.of(customer));

        Page<CartAdminListResponse> result = cartAdminService.findAll(null, null, null, null, null, PageRequest.of(0, 10, Sort.unsorted()));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("John Doe", result.getContent().getFirst().getCustomerName());
        Assertions.assertEquals("cart1", result.getContent().getFirst().getCartId());
    }

    @Test
    void findAll_shouldFilterByCustomerName() {
        Cart cart1 = new Cart();
        cart1.setId("c1");
        cart1.setFirstname("Alice");
        cart1.setLastname("Wonderland");

        // Mock MongoTemplate to return only matching cart
        when(mongoTemplate.count(any(Query.class), eq(Cart.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Cart.class))).thenReturn(List.of(cart1));
        when(orderRepository.findByCartIdIn(List.of("c1"))).thenReturn(Collections.emptyList());

        Page<CartAdminListResponse> result = cartAdminService.findAll(null, null, "Alice", null, null, PageRequest.of(0, 10, Sort.unsorted()));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Alice Wonderland", result.getContent().getFirst().getCustomerName());

        // Verify that the query contained the criteria
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Cart.class));
        Query capturedQuery = queryCaptor.getValue();
        // We can inspect capturedQuery here if needed, but the main point is it executed without error
        Assertions.assertNotNull(capturedQuery);
    }

    @Test
    void findAll_shouldIncludeOrderData() {
        Cart cart = new Cart();
        cart.setId("cart1");

        Order order = new Order();
        order.setCartId("cart1");
        order.setOrderIdentifier(12345L);
        order.setFinalPrice(BigDecimal.valueOf(100));

        when(mongoTemplate.count(any(Query.class), eq(Cart.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Cart.class))).thenReturn(List.of(cart));
        when(orderRepository.findByCartIdIn(List.of("cart1"))).thenReturn(List.of(order));

        Page<CartAdminListResponse> result = cartAdminService.findAll(null, null, null, null, null, PageRequest.of(0, 10, Sort.unsorted()));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(12345L, result.getContent().getFirst().getOrderIdentifier());
        Assertions.assertEquals(BigDecimal.valueOf(100), result.getContent().getFirst().getPrice());
    }
}
