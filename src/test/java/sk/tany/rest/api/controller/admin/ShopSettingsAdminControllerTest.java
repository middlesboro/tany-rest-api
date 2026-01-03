package sk.tany.rest.api.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.component.JwtUtil;
import sk.tany.rest.api.dto.admin.shopsettings.create.ShopSettingsCreateRequest;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.list.ShopSettingsListResponse;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;
import sk.tany.rest.api.service.admin.ShopSettingsAdminService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShopSettingsAdminController.class)
@ActiveProfiles("test")
public class ShopSettingsAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShopSettingsAdminService service;

    @MockBean
    private JwtUtil jwtUtil; // Required because SecurityConfig is loaded

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list() throws Exception {
        when(service.list()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/shop-settings"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create() throws Exception {
        ShopSettingsCreateRequest request = new ShopSettingsCreateRequest();
        when(service.create(any())).thenReturn(new ShopSettingsGetResponse());

        mockMvc.perform(post("/api/admin/shop-settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById() throws Exception {
        String id = "1";
        when(service.get(id)).thenReturn(new ShopSettingsGetResponse());

        mockMvc.perform(get("/api/admin/shop-settings/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update() throws Exception {
        String id = "1";
        ShopSettingsUpdateRequest request = new ShopSettingsUpdateRequest();
        when(service.update(eq(id), any())).thenReturn(new ShopSettingsGetResponse());

        mockMvc.perform(put("/api/admin/shop-settings/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteById() throws Exception {
        String id = "1";

        mockMvc.perform(delete("/api/admin/shop-settings/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }
}
