package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.dto.admin.product.create.ProductCreateRequest;
import sk.tany.rest.api.dto.admin.product.create.ProductCreateResponse;
import sk.tany.rest.api.dto.admin.product.get.ProductGetResponse;
import sk.tany.rest.api.dto.admin.product.list.ProductListResponse;
import sk.tany.rest.api.dto.admin.product.search.ProductSearchResponse;
import sk.tany.rest.api.dto.admin.product.update.ProductUpdateRequest;
import sk.tany.rest.api.dto.admin.product.update.ProductUpdateResponse;
import sk.tany.rest.api.dto.admin.product.upload.ProductUploadImageResponse;
import sk.tany.rest.api.mapper.ProductAdminApiMapper;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.enums.ImageKitType;
import sk.tany.rest.api.service.admin.ProductAdminService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductAdminService productService;
    private final ImageService imageService;
    private final ProductAdminApiMapper productAdminApiMapper;
    private final PrestaShopImportService prestaShopImportService;

    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(@RequestBody ProductCreateRequest product) {
        ProductDto productDto = productAdminApiMapper.toDto(product);
        ProductDto savedProduct = productService.save(productDto);
        return new ResponseEntity<>(productAdminApiMapper.toCreateResponse(savedProduct), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<ProductListResponse> getProducts(Pageable pageable) {
        return productService.findAll(pageable)
                .map(productAdminApiMapper::toListResponse);
    }

    @GetMapping("/search")
    public Page<ProductSearchResponse> search(@RequestParam String categoryId, Pageable pageable) {
        return productService.search(categoryId, pageable)
                .map(productAdminApiMapper::toSearchResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductGetResponse> getProduct(@PathVariable String id) {
        return productService.findById(id)
                .map(productAdminApiMapper::toGetResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductUpdateResponse> updateProduct(@PathVariable String id, @RequestBody ProductUpdateRequest product) {
        ProductDto productDto = productAdminApiMapper.toDto(product);
        ProductDto updatedProduct = productService.update(id, productDto);
        return ResponseEntity.ok(productAdminApiMapper.toUpdateResponse(updatedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ProductUploadImageResponse> uploadImages(@PathVariable String id, @RequestParam("files") MultipartFile[] files) {
        return productService.findById(id)
                .map(product -> {
                    List<String> imageUrls = Arrays.stream(files)
                            .map(file -> imageService.upload(file, ImageKitType.PRODUCT))
                            .toList();

                    if (product.getImages() == null) {
                        product.setImages(new ArrayList<>());
                    }
                    product.getImages().addAll(imageUrls);

                    ProductDto updatedProduct = productService.update(id, product);
                    return ResponseEntity.ok(productAdminApiMapper.toUploadImageResponse(updatedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/import/prestashop")
    public ResponseEntity<Void> importAllProducts() {
        prestaShopImportService.importAllProducts();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import/prestashop/{id}")
    public ResponseEntity<Void> importProduct(@PathVariable String id) {
        prestaShopImportService.importProduct(id);
        return ResponseEntity.ok().build();
    }
}
