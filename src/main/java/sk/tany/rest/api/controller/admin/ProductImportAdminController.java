package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.service.admin.ProductImportService;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
public class ProductImportAdminController {

    private final ProductImportService productImportService;

    @Operation(summary = "Import products from JSON")
    @PostMapping("/products")
    public void importProducts() {
        productImportService.importProducts();
    }
}
