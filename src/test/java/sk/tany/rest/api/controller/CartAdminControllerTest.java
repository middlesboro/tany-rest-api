package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sk.tany.rest.api.controller.admin.CartAdminController;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.dto.CartAdminResponse;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.service.CartService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CartAdminControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CartAdminController cartAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCarts_ShouldReturnCorrectAttributes() {
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

        // Verify requested attributes
        assertEquals(cartId, item.getCartId());
        assertEquals(customerId, item.getCustomerId());
        assertEquals("John Doe", item.getCustomerName());
        assertEquals(now, item.getCreateDate());
        assertEquals(now, item.getUpdateDate());
    }

    @Test
    void getCartById_ShouldReturnCartWithItems() {
        // Arrange
        String cartId = "cart-456";
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);

        CartItem cartItem = new CartItem();
        cartItem.setProductId("prod-1");
        cartItem.setQuantity(2);
        cartItem.setTitle("Product Title");
        cartItem.setImage("http://image.url");
        cartItem.setPrice(BigDecimal.valueOf(100.50));

        cartDto.setItems(Collections.singletonList(cartItem));

        when(cartService.findById(cartId)).thenReturn(Optional.of(cartDto));

        // Act
        ResponseEntity<CartDto> response = cartAdminController.getCartById(cartId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        CartDto responseBody = response.getBody();
        assertEquals(cartId, responseBody.getCartId());

        // Verify items and their attributes
        assertNotNull(responseBody.getItems());
        assertEquals(1, responseBody.getItems().size());
        CartItem item = responseBody.getItems().get(0);

        assertEquals("prod-1", item.getProductId());
        assertEquals(2, item.getQuantity());
        assertEquals("Product Title", item.getTitle());
        assertEquals("http://image.url", item.getImage());
        assertEquals(BigDecimal.valueOf(100.50), item.getPrice());
    }
}
