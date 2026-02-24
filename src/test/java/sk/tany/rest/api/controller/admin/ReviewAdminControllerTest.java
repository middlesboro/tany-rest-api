package sk.tany.rest.api.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.config.PageSerializationAdvice;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.dto.admin.review.ReviewAdminCreateRequest;
import sk.tany.rest.api.dto.admin.review.ReviewAdminDetailResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminUpdateRequest;
import sk.tany.rest.api.service.admin.ReviewAdminService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewAdminController.class)
@Import(PageSerializationAdvice.class)
class ReviewAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewAdminService reviewAdminService;

    @MockitoBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @MockitoBean
    private sk.tany.rest.api.config.CorsConfig corsConfig;

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_ShouldReturnPageOfReviews() throws Exception {
        when(reviewAdminService.findAll(any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/admin/reviews")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_ShouldReturnReview() throws Exception {
        String id = "1";
        ReviewAdminDetailResponse response = new ReviewAdminDetailResponse();
        response.setId(id);
        when(reviewAdminService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/admin/reviews/{id}", id)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_ShouldReturnCreatedReview() throws Exception {
        ReviewAdminDetailResponse response = new ReviewAdminDetailResponse();
        response.setId("1");
        when(reviewAdminService.create(any(ReviewAdminCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"123\", \"title\":\"Test\", \"rating\":5}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_ShouldReturnUpdatedReview() throws Exception {
        String id = "1";
        ReviewAdminDetailResponse response = new ReviewAdminDetailResponse();
        response.setId(id);
        when(reviewAdminService.update(eq(id), any(ReviewAdminUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/admin/reviews/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_ShouldDeleteReview() throws Exception {
        String id = "1";
        mockMvc.perform(delete("/api/admin/reviews/{id}", id)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reviewAdminService).delete(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void importReviews_ShouldCallService() throws Exception {
        mockMvc.perform(post("/api/admin/reviews/import")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(reviewAdminService).importReviews();
    }
}
