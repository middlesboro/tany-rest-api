package sk.tany.rest.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.service.ProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto product) {
        ProductDto savedProduct = productService.save(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<ProductDto> getProducts(Pageable pageable) {
        return productService.findAll(pageable);
    }

    @GetMapping("/search")
    public Page<ProductDto> search(@RequestParam String categoryId, Pageable pageable) {
        return productService.search(categoryId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable String id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable String id, @RequestBody ProductDto product) {
        ProductDto updatedProduct = productService.update(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
