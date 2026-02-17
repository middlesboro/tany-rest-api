package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.dto.admin.product.ProductAdminDto;
import sk.tany.rest.api.dto.admin.product.create.ProductCreateRequest;
import sk.tany.rest.api.dto.admin.product.create.ProductCreateResponse;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;
import sk.tany.rest.api.dto.admin.product.get.ProductGetResponse;
import sk.tany.rest.api.dto.admin.product.list.ProductListResponse;
import sk.tany.rest.api.dto.admin.product.patch.ProductPatchRequest;
import sk.tany.rest.api.dto.admin.product.search.ProductSearchResponse;
import sk.tany.rest.api.dto.admin.product.update.ProductUpdateRequest;
import sk.tany.rest.api.dto.admin.product.update.ProductUpdateResponse;
import sk.tany.rest.api.dto.admin.product.upload.ProductUploadImageResponse;
import sk.tany.rest.api.mapper.ProductAdminApiMapper;
import sk.tany.rest.api.service.admin.PrestaShopImportService;
import sk.tany.rest.api.service.admin.ProductAdminService;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.service.common.ProductEmbeddingService;
import sk.tany.rest.api.service.common.enums.ImageKitType;
import sk.tany.rest.api.service.scheduler.InvoiceUploadScheduler;

import java.math.BigDecimal;
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
    private final InvoiceUploadScheduler invoiceUploadScheduler;
    private final ProductEmbeddingService productEmbeddingService;

    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(@RequestBody ProductCreateRequest product) {
        ProductAdminDto productDto = productAdminApiMapper.toDto(product);
        ProductAdminDto savedProduct = productService.save(productDto);
        return new ResponseEntity<>(productAdminApiMapper.toCreateResponse(savedProduct), HttpStatus.CREATED);
    }

    @PostMapping("/generate-missing-slugs")
    public ResponseEntity<Void> generateMissingSlugs() {
        productService.generateMissingSlugs();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/quantity")
    public ResponseEntity<Void> updateAllProductsQuantity(@RequestParam Integer value) {
        productService.updateAllProductsQuantity(value);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public Page<ProductListResponse> getProducts(@RequestParam(value = "query", required = false) String query, @RequestParam(value = "priceFrom", required = false) BigDecimal priceFrom,
                                                 @RequestParam(value = "priceTo", required = false) BigDecimal priceTo, @RequestParam(value = "brandId", required = false) String brandId,
                                                 @RequestParam(value = "id", required = false) String id, @RequestParam(value = "externalStock", required = false) Boolean externalStock,
                                                 @RequestParam(value = "quantity", required = false) Integer quantity, @RequestParam(value = "productIdentifier", required = false) Long productIdentifier,
                                                 @RequestParam(value = "active", required = false) Boolean active, Pageable pageable) {
        return productService.findAll(new ProductFilter(query, priceFrom, priceTo, brandId, id, externalStock, quantity, productIdentifier, active), pageable)
                .map(productAdminApiMapper::toListResponse);
    }

    @GetMapping("/search")
    public Page<ProductSearchResponse> search(@RequestParam String categoryId, Pageable pageable) {
        return productService.search(categoryId, pageable)
                .map(productAdminApiMapper::toSearchResponse);
    }

    @GetMapping(value = "/search", params = "query")
    public List<ProductSearchResponse> searchByQuery(@RequestParam String query) {
        return productService.searchByQuery(query).stream()
                .map(productAdminApiMapper::toSearchResponse)
                .toList();
    }

    @GetMapping("/by-filter-value/{filterParameterValueId}")
    public List<ProductListResponse> getProductsByFilterParameterValueId(@PathVariable String filterParameterValueId) {
        return productService.findAllByFilterParameterValueId(filterParameterValueId).stream()
                .map(productAdminApiMapper::toListResponse)
                .toList();
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
        ProductAdminDto productDto = productAdminApiMapper.toDto(product);
        ProductAdminDto updatedProduct = productService.update(id, productDto);
        return ResponseEntity.ok(productAdminApiMapper.toUpdateResponse(updatedProduct));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductUpdateResponse> patchProduct(@PathVariable String id, @RequestBody ProductPatchRequest patchDto) {
        ProductAdminDto updatedProduct = productService.patch(id, patchDto);
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

                    ProductAdminDto updatedProduct = productService.update(id, product);
                    return ResponseEntity.ok(productAdminApiMapper.toUploadImageResponse(updatedProduct));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/images")
    public ResponseEntity<Void> deleteImage(@PathVariable String id, @RequestParam String url) {
        return productService.findById(id)
                .map(product -> {
                    if (product.getImages() != null && product.getImages().remove(url)) {
                        imageService.delete(url);
                        productService.update(id, product);
                    }
                    return ResponseEntity.noContent().<Void>build();
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

    @PostMapping("/export/invoice/onedrive")
    public ResponseEntity<Void> exportInvoiceToOnedrive() {
        invoiceUploadScheduler.processUploads();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/embeddings/init")
    public ResponseEntity<Void> initEmbeddings() {
        productEmbeddingService.reEmbedAllProducts();
        return ResponseEntity.ok().build();
    }
}
