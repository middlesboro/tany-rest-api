package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.domain.category.Category;
import sk.tany.rest.api.domain.category.CategoryRepository;
import sk.tany.rest.api.dto.client.product.ProductClientSearchDto;
import sk.tany.rest.api.dto.client.product.get.ProductClientGetResponse;
import sk.tany.rest.api.dto.client.product.list.ProductClientListResponse;
import sk.tany.rest.api.dto.client.product.search.ProductClientSearchResponse;
import sk.tany.rest.api.dto.request.CategoryFilterRequest;
import sk.tany.rest.api.mapper.ProductClientApiMapper;
import sk.tany.rest.api.service.client.ProductClientService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductClientController {

    private final ProductClientService productService;
    private final ProductClientApiMapper productClientApiMapper;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public Page<ProductClientListResponse> getProducts(Pageable pageable) {
        return productService.findAll(pageable)
                .map(productClientApiMapper::toListResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductClientGetResponse> getProduct(@PathVariable String id) {
        return productService.findById(id)
                .map(productClientApiMapper::toGetResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductClientGetResponse> getProductBySlug(@PathVariable String slug) {
        return productService.findBySlug(slug)
                .map(productClientApiMapper::toGetResponse)
                .map(response -> {
                    if (response.getDefaultCategoryId() != null) {
                        categoryRepository.findById(response.getDefaultCategoryId())
                                .ifPresent(category -> {
                                    response.setDefaultCategoryTitle(category.getTitle());
                                });
                    }
                    return response;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/related")
    public java.util.List<ProductClientListResponse> getRelatedProducts(@PathVariable String id) {
        return productService.getRelatedProducts(id)
                .stream()
                .map(productClientApiMapper::toListResponse)
                .toList();
    }

    @GetMapping("/category/{categoryId}")
    public Page<ProductClientListResponse> getProductsByCategory(@PathVariable String categoryId, Pageable pageable) {
        return productService.search(categoryId, pageable)
                .map(productClientApiMapper::toListResponse);
    }

    @PostMapping("/category/{categoryId}/search")
    public ProductClientSearchResponse searchProductsByCategory(@PathVariable String categoryId, @RequestBody CategoryFilterRequest request, Pageable pageable) {
        ProductClientSearchDto result = productService.search(categoryId, request, pageable);

        ProductClientSearchResponse response = new ProductClientSearchResponse();
        response.setProducts(result.getProducts().map(productClientApiMapper::toListResponse));
        response.setFilterParameters(result.getFilterParameters());
        return response;
    }

    @GetMapping("/search")
    public java.util.List<ProductClientListResponse> searchProducts(@RequestParam String query) {
        return productService.searchProducts(query)
                .stream()
                .map(productClientApiMapper::toListResponse)
                .toList();
    }

}
