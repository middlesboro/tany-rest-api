package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.domain.jwk.JwkKeyRepository;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateRequest;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateResponse;
import sk.tany.rest.api.mapper.CartClientApiMapper;
import sk.tany.rest.api.service.client.CartClientService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartClientController.class)
class CartClientControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartClientService cartService;

    @MockitoBean
    private CartClientApiMapper cartClientApiMapper;

    @MockitoBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockitoBean
    private JwkKeyRepository jwkKeyRepository;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @Test
    @WithMockUser
    void updateCart_WhenCartIdIsBlank_ShouldReturnBadRequest() throws Exception {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("");

        mockMvc.perform(put("/api/cart")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateCart_WhenEmailIsInvalid_ShouldReturnBadRequest() throws Exception {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-1");
        request.setEmail("invalid-email");

        mockMvc.perform(put("/api/cart")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateCart_WhenItemQuantityIsInvalid_ShouldReturnBadRequest() throws Exception {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-1");
        CartClientUpdateRequest.CartItem item = new CartClientUpdateRequest.CartItem("prod-1", 0);
        request.setItems(List.of(item));

        mockMvc.perform(put("/api/cart")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateCart_WhenAddressIsInvalid_ShouldReturnBadRequest() throws Exception {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-1");
        AddressDto address = new AddressDto();
        address.setStreet(""); // Invalid
        address.setCity("City");
        address.setZip("12345");
        address.setCountry("Country");
        request.setInvoiceAddress(address);

        mockMvc.perform(put("/api/cart")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void updateCart_WhenValid_ShouldReturnOk() throws Exception {
        CartClientUpdateRequest request = new CartClientUpdateRequest();
        request.setCartId("cart-1");
        request.setEmail("test@example.com");

        CartDto cartDto = new CartDto();
        cartDto.setCartId("cart-1");

        when(cartService.getOrCreateCart(eq("cart-1"), any())).thenReturn(cartDto);
        when(cartService.save(any(CartDto.class))).thenReturn(cartDto);
        when(cartClientApiMapper.toUpdateResponse(any(CartDto.class))).thenReturn(new CartClientUpdateResponse());

        mockMvc.perform(put("/api/cart")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
