package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.ProductDto;

import java.util.Optional;

public interface ProductAdminService {
    Page<ProductDto> findAll(Pageable pageable);
    Optional<ProductDto> findById(String id);
    ProductDto save(ProductDto productDto);
    ProductDto update(String id, ProductDto productDto);
    void deleteById(String id);
    Page<ProductDto> search(String categoryId, Pageable pageable);
    java.util.List<ProductDto> findAllByIds(Iterable<String> ids);
}
