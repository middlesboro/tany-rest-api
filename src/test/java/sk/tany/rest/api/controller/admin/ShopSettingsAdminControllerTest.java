package sk.tany.rest.api.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.dto.admin.shopsettings.get.ShopSettingsGetResponse;
import sk.tany.rest.api.dto.admin.shopsettings.update.ShopSettingsUpdateRequest;
import sk.tany.rest.api.service.admin.ShopSettingsAdminService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShopSettingsAdminController.class)
@ActiveProfiles("test")
public class ShopSettingsAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShopSettingsAdminService service;

    @MockitoBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSettings() throws Exception {
        when(service.get()).thenReturn(new ShopSettingsGetResponse());

        mockMvc.perform(get("/api/admin/shop-settings"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update() throws Exception {
        ShopSettingsUpdateRequest request = new ShopSettingsUpdateRequest();
        when(service.update(any())).thenReturn(new ShopSettingsGetResponse());

        mockMvc.perform(put("/api/admin/shop-settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
