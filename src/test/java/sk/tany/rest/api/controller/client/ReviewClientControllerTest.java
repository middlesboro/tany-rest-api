package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.domain.jwk.JwkKeyRepository;
import sk.tany.rest.api.dto.client.review.ReviewClientListResponse;
import sk.tany.rest.api.dto.client.review.ReviewClientProductResponse;
import sk.tany.rest.api.service.client.ReviewClientService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewClientController.class)
class ReviewClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewClientService reviewClientService;

    @MockBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockBean
    private JwkKeyRepository jwkKeyRepository;

    @MockBean
    private SecurityContextRepository securityContextRepository;

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
}
