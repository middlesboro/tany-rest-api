package sk.tany.rest.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import sk.tany.rest.api.component.JwtUtil;
import sk.tany.rest.api.controller.admin.BrandAdminController;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.service.admin.BrandAdminService;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.common.ImageService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrandAdminController.class)
class BrandAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrandAdminService brandService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private PrestaShopImportService prestaShopImportService;

    @MockBean
    private JwtUtil jwtUtil;

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
}
