package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.dto.client.product.ProductClientSearchDto;

import java.util.Optional;

public interface ProductClientService {
    Page<ProductClientDto> findAll(Pageable pageable);
    Optional<ProductClientDto> findById(String id);
    Optional<ProductClientDto> findBySlug(String slug);
    Page<ProductClientDto> search(String categoryId, Pageable pageable);
    ProductClientSearchDto search(String categoryId, sk.tany.rest.api.dto.request.CategoryFilterRequest request, Pageable pageable);
    java.util.List<ProductClientDto> findAllByIds(Iterable<String> ids);
    java.util.List<ProductClientDto> searchProducts(String query);
    java.util.List<ProductClientDto> getRelatedProducts(String productId);

    void updateProductStock(String productId, Integer quantityChange);
}
