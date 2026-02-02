package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.domain.brand.BrandRepository;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.service.admin.ProductImportService;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
public class ProductImportAdminController {

    private final ProductImportService productImportService;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ReviewRepository reviewRepository;

    @Operation(summary = "Import products from JSON")
    @PostMapping("/products")
    public void importProducts() {
        productImportService.importProducts();
    }

    // TODO later remove
    @Operation(summary = "Delete all products")
    @DeleteMapping("/delete-all-products")
    public void deleteProducts() {
        productRepository.deleteAll();
//        brandRepository.deleteAll();
        reviewRepository.deleteAll();
    }

}
