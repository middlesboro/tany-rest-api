package sk.tany.rest.api.controller.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.config.security.MagicLinkLoginFilter;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.jwk.JwkKeyRepository;
import sk.tany.rest.api.dto.client.review.ReviewClientCreateRequest;
import sk.tany.rest.api.dto.client.review.ReviewClientProductResponse;
import sk.tany.rest.api.service.client.ReviewClientService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewClientController.class)
class ReviewClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewClientService reviewClientService;

    @MockitoBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockitoBean
    private JwkKeyRepository jwkKeyRepository;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @MockitoBean
    private MagicLinkTokenRepository magicLinkTokenRepository;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private MagicLinkLoginFilter magicLinkLoginFilter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(magicLinkLoginFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser
    void findAllByProductId_ShouldReturnPageOfReviews() throws Exception {
        String productId = "123";
        ReviewClientProductResponse response = new ReviewClientProductResponse();
        response.setReviews(new PageImpl<>(Collections.emptyList()));
        when(reviewClientService.findAllByProductId(eq(productId), any())).thenReturn(response);

        mockMvc.perform(get("/api/reviews/product/{productId}", productId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void create_WhenInvalidRequest_ShouldReturnBadRequest() throws Exception {
        ReviewClientCreateRequest request = new ReviewClientCreateRequest();
        // Missing required fields

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_WhenValidRequest_ShouldReturnOk() throws Exception {
        ReviewClientCreateRequest request = new ReviewClientCreateRequest();
        request.setProductId("prod-1");
        request.setText("Great product!");
        request.setRating(5);
        request.setTitle("Awesome");
        request.setEmail("user@example.com");

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
