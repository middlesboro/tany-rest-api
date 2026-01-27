package sk.tany.rest.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.controller.admin.CartAdminController;
import sk.tany.rest.api.dto.admin.cart.list.CartAdminListResponse;
import sk.tany.rest.api.service.admin.CartAdminService;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CartAdminControllerTest {

    @Mock
    private CartAdminService cartService;

    @InjectMocks
    private CartAdminController cartAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCarts_ShouldReturnPageOfResponses() {
        // Arrange
        Instant now = Instant.now();
        String customerId = "cust-123";
        String cartId = "cart-456";

        CartAdminListResponse listResponse = new CartAdminListResponse();
        listResponse.setCartId(cartId);
        listResponse.setCustomerId(customerId);
        listResponse.setCustomerName("John Doe");
        listResponse.setCreateDate(now);
        listResponse.setUpdateDate(now);

        Page<CartAdminListResponse> page = new PageImpl<>(Collections.singletonList(listResponse));

        when(cartService.findAll(any(), any(), any(), any(), any(), any())).thenReturn(page);

        // Act
        Page<CartAdminListResponse> result = cartAdminController.getAllCarts(null, null, null, null, null, Pageable.unpaged());

        // Assert
        assertEquals(1, result.getTotalElements());
        CartAdminListResponse item = result.getContent().get(0);
        assertEquals(cartId, item.getCartId());
        assertEquals(customerId, item.getCustomerId());
        assertEquals("John Doe", item.getCustomerName());
    }
}
