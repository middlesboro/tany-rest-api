package sk.tany.rest.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import sk.tany.rest.api.controller.admin.BrandAdminController;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.service.admin.BrandAdminService;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.common.ImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Import;
import sk.tany.rest.api.config.SecurityConfig;
import sk.tany.rest.api.config.SecurityProperties;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import sk.tany.rest.api.config.security.MagicLinkAuthenticationProvider;
import sk.tany.rest.api.domain.jwk.JwkKeyRepository;
import sk.tany.rest.api.dto.admin.brand.patch.BrandPatchRequest;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrandAdminController.class)
@Import({SecurityConfig.class, SecurityProperties.class})
class BrandAdminControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrandAdminService brandService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private PrestaShopImportService prestaShopImportService;

    @MockBean
    private MagicLinkAuthenticationProvider magicLinkAuthenticationProvider;

    @MockBean
    private JwkKeyRepository jwkKeyRepository;

    @MockBean
    private AuthorizationServerSettings authorizationServerSettings;

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadImage_ShouldReturnUpdatedBrand_WhenBrandExists() throws Exception {
        String brandId = "1";
        String imageUrl = "http://example.com/image.jpg";
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        BrandDto brandDto = new BrandDto();
        brandDto.setId(brandId);
        brandDto.setName("Test Brand");

        BrandDto updatedBrandDto = new BrandDto();
        updatedBrandDto.setId(brandId);
        updatedBrandDto.setName("Test Brand");
        updatedBrandDto.setImage(imageUrl);

        when(brandService.findById(brandId)).thenReturn(Optional.of(brandDto));
        when(imageService.upload(any(), any())).thenReturn(imageUrl);
        when(brandService.update(eq(brandId), any(BrandDto.class))).thenReturn(updatedBrandDto);

        mockMvc.perform(multipart("/api/admin/brands/{id}/image", brandId)
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value(imageUrl));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadImage_ShouldReturnNotFound_WhenBrandDoesNotExist() throws Exception {
        String brandId = "999";
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        when(brandService.findById(brandId)).thenReturn(Optional.empty());

        mockMvc.perform(multipart("/api/admin/brands/{id}/image", brandId)
                .file(file)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void patchBrand_shouldReturnUpdatedBrand() throws Exception {
        String brandId = "brand123";
        BrandPatchRequest patchRequest = new BrandPatchRequest();
        patchRequest.setName("New Brand Name");

        BrandDto updatedBrand = new BrandDto();
        updatedBrand.setId(brandId);
        updatedBrand.setName("New Brand Name");
        updatedBrand.setImage("old-image.jpg");

        when(brandService.patch(eq(brandId), eq(patchRequest))).thenReturn(updatedBrand);

        mockMvc.perform(patch("/api/admin/brands/{id}", brandId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(brandId))
                .andExpect(jsonPath("$.name").value("New Brand Name"))
                .andExpect(jsonPath("$.image").value("old-image.jpg"));
    }
}
