package sk.tany.rest.api.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sk.tany.rest.api.component.JwtUtil;
import sk.tany.rest.api.dto.SupplierDto;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.admin.SupplierAdminService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierAdminController.class)
class SupplierAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupplierAdminService supplierService;

    @MockBean
    private PrestaShopImportService prestaShopImportService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void importFromPrestaShop_ShouldCallService() throws Exception {
        mockMvc.perform(post("/api/admin/suppliers/import/prestashop")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(prestaShopImportService).importAllSuppliers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSupplier_ShouldReturnCreatedSupplier() throws Exception {
        SupplierDto supplierDto = new SupplierDto();
        supplierDto.setName("Test Supplier");

        SupplierDto savedSupplierDto = new SupplierDto();
        savedSupplierDto.setId("1");
        savedSupplierDto.setName("Test Supplier");

        when(supplierService.save(any(SupplierDto.class))).thenReturn(savedSupplierDto);

        mockMvc.perform(post("/api/admin/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Supplier\"}")
                .with(csrf()))
                .andExpect(status().isCreated());
    }
}
