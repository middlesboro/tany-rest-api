package sk.tany.rest.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.component.JwtUtil;
import sk.tany.rest.api.controller.client.CartClientController;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.client.cart.add.CartClientAddItemRequest;
import sk.tany.rest.api.dto.client.cart.remove.CartClientRemoveItemRequest;
import sk.tany.rest.api.dto.client.cart.carrier.CartClientSetCarrierRequest;
import sk.tany.rest.api.dto.client.cart.carrier.CartClientSetCarrierResponse;
import sk.tany.rest.api.dto.client.cart.payment.CartClientSetPaymentRequest;
import sk.tany.rest.api.dto.client.cart.payment.CartClientSetPaymentResponse;
import sk.tany.rest.api.mapper.CartClientApiMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.tany.rest.api.service.client.CartClientService;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(CartClientController.class)
public class CartClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartClientService cartService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CartClientApiMapper cartClientApiMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void addProduct_shouldReturnCartId() throws Exception {
        String cartId = "cart-123";
        String productId = "prod-456";
        Integer quantity = 1;
        CartClientAddItemRequest request = new CartClientAddItemRequest();
        request.setCartId(cartId);
        request.setProductId(productId);
        request.setQuantity(quantity);

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        given(cartService.getOrCreateCart(cartId, null)).willReturn(cartDto);
        given(cartService.addProductToCart(cartId, productId, quantity)).willReturn(cartId);

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"cartId\":\"cart-123\"}"));
    }

    @Test
    @WithMockUser
    public void removeProduct_shouldReturnCartId() throws Exception {
        String cartId = "cart-123";
        String productId = "prod-456";
        CartClientRemoveItemRequest request = new CartClientRemoveItemRequest();
        request.setCartId(cartId);
        request.setProductId(productId);

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        given(cartService.getOrCreateCart(cartId, null)).willReturn(cartDto);
        given(cartService.removeProductFromCart(cartId, productId)).willReturn(cartId);

        mockMvc.perform(delete("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"cartId\":\"cart-123\"}"));
    }

    @Test
    @WithMockUser
    public void addCarrier_shouldReturnUpdatedCart() throws Exception {
        String cartId = "cart-123";
        String carrierId = "carrier-456";
        CartClientSetCarrierRequest request = new CartClientSetCarrierRequest();
        request.setCartId(cartId);
        request.setCarrierId(carrierId);

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setSelectedCarrierId(carrierId);

        CartClientSetCarrierResponse response = new CartClientSetCarrierResponse();
        response.setCartId(cartId);
        response.setSelectedCarrierId(carrierId);

        given(cartService.addCarrier(cartId, carrierId)).willReturn(cartDto);
        given(cartClientApiMapper.toSetCarrierResponse(any(CartDto.class))).willReturn(response);

        mockMvc.perform(post("/api/cart/carrier")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.selectedCarrierId").value(carrierId));
    }

    @Test
    @WithMockUser
    public void addPayment_shouldReturnUpdatedCart() throws Exception {
        String cartId = "cart-123";
        String paymentId = "payment-789";
        CartClientSetPaymentRequest request = new CartClientSetPaymentRequest();
        request.setCartId(cartId);
        request.setPaymentId(paymentId);

        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setSelectedPaymentId(paymentId);

        CartClientSetPaymentResponse response = new CartClientSetPaymentResponse();
        response.setCartId(cartId);
        response.setSelectedPaymentId(paymentId);

        given(cartService.addPayment(cartId, paymentId)).willReturn(cartDto);
        given(cartClientApiMapper.toSetPaymentResponse(any(CartDto.class))).willReturn(response);

        mockMvc.perform(post("/api/cart/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.selectedPaymentId").value(paymentId));
    }
}
