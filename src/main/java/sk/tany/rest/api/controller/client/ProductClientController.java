package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.dto.client.product.get.ProductClientGetResponse;
import sk.tany.rest.api.dto.client.product.list.ProductClientListResponse;
import sk.tany.rest.api.mapper.ProductClientApiMapper;
import sk.tany.rest.api.service.client.ProductClientService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductClientController {

    private final ProductClientService productService;
    private final ProductClientApiMapper productClientApiMapper;

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

    @GetMapping("/category/{categoryId}")
    public Page<ProductClientListResponse> getProductsByCategory(@PathVariable String categoryId, Pageable pageable) {
        return productService.search(categoryId, pageable)
                .map(productClientApiMapper::toListResponse);
    }

}
