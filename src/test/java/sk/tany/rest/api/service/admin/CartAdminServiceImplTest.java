package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import static org.mockito.ArgumentMatchers.anyString;
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

        when(cartRepository.findAll()).thenReturn(List.of(cart));
        // Mock findByCartId because simple path uses it
        when(orderRepository.findByCartId("cart1")).thenReturn(Optional.empty());
        // Mock findById because simple path uses it
        when(customerRepository.findById("cust1")).thenReturn(Optional.of(customer));

        Page<CartAdminListResponse> result = cartAdminService.findAll(null, null, null, null, null, PageRequest.of(0, 10, Sort.unsorted()));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("John Doe", result.getContent().get(0).getCustomerName());
        Assertions.assertEquals("cart1", result.getContent().get(0).getCartId());
    }

    @Test
    void findAll_shouldFilterByCustomerName() {
        // This test uses a filter "Alice", so it should trigger the Full Join Path.
        // Full Join Path uses findAll()

        Cart cart1 = new Cart();
        cart1.setId("c1");
        cart1.setFirstname("Alice");
        cart1.setLastname("Wonderland");

        Cart cart2 = new Cart();
        cart2.setId("c2");
        cart2.setFirstname("Bob");
        cart2.setLastname("Builder");

        when(cartRepository.findAll()).thenReturn(List.of(cart1, cart2));
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());
        when(carrierRepository.findAll()).thenReturn(Collections.emptyList());
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        Page<CartAdminListResponse> result = cartAdminService.findAll(null, null, "Alice", null, null, PageRequest.of(0, 10, Sort.unsorted()));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Alice Wonderland", result.getContent().get(0).getCustomerName());
    }

    @Test
    void findAll_shouldIncludeOrderData() {
        Cart cart = new Cart();
        cart.setId("cart1");

        Order order = new Order();
        order.setCartId("cart1");
        order.setOrderIdentifier(12345L);
        order.setFinalPrice(BigDecimal.valueOf(100));

        when(cartRepository.findAll()).thenReturn(List.of(cart));
        // Simple Path uses findByCartId
        when(orderRepository.findByCartId("cart1")).thenReturn(Optional.of(order));

        Page<CartAdminListResponse> result = cartAdminService.findAll(null, null, null, null, null, PageRequest.of(0, 10, Sort.unsorted()));

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(12345L, result.getContent().get(0).getOrderIdentifier());
        Assertions.assertEquals(BigDecimal.valueOf(100), result.getContent().get(0).getPrice());
    }
}
