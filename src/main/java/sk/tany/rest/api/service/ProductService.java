package sk.tany.rest.api.service;

import sk.tany.rest.api.dto.ProductDto;

import java.util.Optional;

public interface ProductService {
    Optional<ProductDto> findById(String id);
    ProductDto save(ProductDto productDto);
    ProductDto update(String id, ProductDto productDto);
    void deleteById(String id);
}
