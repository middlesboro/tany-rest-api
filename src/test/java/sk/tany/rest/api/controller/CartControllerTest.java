package sk.tany.rest.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.component.JwtUtil;
import sk.tany.rest.api.dto.CartItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.tany.rest.api.service.client.CartClientService;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartClientService cartService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void addProduct_shouldReturnCartId() throws Exception {
        String cartId = "cart-123";
        String productId = "prod-456";
        Integer quantity = 1;
        CartItemRequest request = new CartItemRequest();
        request.setCartId(cartId);
        request.setProductId(productId);
        request.setQuantity(quantity);

        given(cartService.addProductToCart(cartId, productId, quantity)).willReturn(cartId);

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"cartId\":\"cart-123\"}"));
    }
}
