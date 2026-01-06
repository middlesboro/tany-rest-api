package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.ProductDto;

import java.util.Optional;

public interface ProductClientService {
    Page<ProductDto> findAll(Pageable pageable);
    Optional<ProductDto> findById(String id);
    Page<ProductDto> search(String categoryId, Pageable pageable);
    java.util.List<ProductDto> findAllByIds(Iterable<String> ids);
    java.util.List<ProductDto> searchProducts(String query);
}
