package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.controller.admin.CartAdminController;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CartAdminResponse;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.service.admin.CartAdminService;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CartAdminControllerTest {

    @Mock
    private CartAdminService cartService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CartAdminController cartAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCarts_ShouldReturnEnrichedResponse() {
        // Arrange
        Instant now = Instant.now();
        String customerId = "cust-123";
        String cartId = "cart-456";

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setCustomerId(customerId);
        cartDto.setCreateDate(now);
        cartDto.setUpdateDate(now);

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstname("John");
        customer.setLastname("Doe");

        when(cartService.findAll()).thenReturn(Collections.singletonList(cartDto));
        when(customerRepository.findAllById(any())).thenReturn(Collections.singletonList(customer));

        // Act
        List<CartAdminResponse> response = cartAdminController.getAllCarts();

        // Assert
        assertEquals(1, response.size());
        CartAdminResponse item = response.get(0);
        assertEquals(cartId, item.getCartId());
        assertEquals(customerId, item.getCustomerId());
        assertEquals("John Doe", item.getCustomerName());
        assertEquals(now, item.getCreateDate());
        assertEquals(now, item.getUpdateDate());
    }
}
