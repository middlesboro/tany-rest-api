package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;

import java.util.Optional;

public interface ProductAdminService {
    Page<ProductDto> findAll(Pageable pageable);
    Page<ProductDto> findAll(ProductFilter filter, Pageable pageable);
    Optional<ProductDto> findById(String id);
    ProductDto save(ProductDto productDto);
    ProductDto update(String id, ProductDto productDto);
    ProductDto patch(String id, sk.tany.rest.api.dto.admin.product.patch.ProductPatchRequest patchDto);
    void deleteById(String id);
    Page<ProductDto> search(String categoryId, Pageable pageable);
    java.util.List<ProductDto> searchByQuery(String query);
    java.util.List<ProductDto> findAllByIds(Iterable<String> ids);
    java.util.List<ProductDto> findAllByFilterParameterValueId(String filterParameterValueId);
}
