package sk.tany.rest.api.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.component.SecurityUtil;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.domain.jwk.JwkKeyRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateRequest;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateResponse;
import sk.tany.rest.api.mapper.OrderClientApiMapper;
import sk.tany.rest.api.service.client.OrderClientService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderClientController.class)
class OrderClientControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderClientService orderClientService;

    @MockBean
    private OrderClientApiMapper orderClientApiMapper;

    @MockBean
    private SecurityUtil securityUtil;

    @MockBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockBean
    private JwkKeyRepository jwkKeyRepository;

    @MockBean
    private SecurityContextRepository securityContextRepository;

    @Test
    @WithMockUser
    void createOrder_WhenCartIdIsNull_ShouldReturnBadRequest() throws Exception {
        OrderClientCreateRequest request = new OrderClientCreateRequest();
        request.setCartId(null);

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createOrder_WhenCartIdIsBlank_ShouldReturnBadRequest() throws Exception {
        OrderClientCreateRequest request = new OrderClientCreateRequest();
        request.setCartId("");

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createOrder_WhenValid_ShouldReturnOk() throws Exception {
        OrderClientCreateRequest request = new OrderClientCreateRequest();
        request.setCartId("cart-123");

        OrderDto orderDto = new OrderDto();
        OrderClientCreateResponse response = new OrderClientCreateResponse();

        when(orderClientApiMapper.toDto(any(OrderClientCreateRequest.class))).thenReturn(orderDto);
        when(orderClientService.createOrder(any(OrderDto.class))).thenReturn(orderDto);
        when(orderClientApiMapper.toCreateResponse(any(OrderDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
