package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.admin.product.ProductAdminDto;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;

import java.util.Optional;

public interface ProductAdminService {
    Page<ProductAdminDto> findAll(Pageable pageable);
    Page<ProductAdminDto> findAll(ProductFilter filter, Pageable pageable);
    Optional<ProductAdminDto> findById(String id);
    ProductAdminDto save(ProductAdminDto productDto);
    ProductAdminDto update(String id, ProductAdminDto productDto);
    ProductAdminDto patch(String id, sk.tany.rest.api.dto.admin.product.patch.ProductPatchRequest patchDto);
    void deleteById(String id);
    Page<ProductAdminDto> search(String categoryId, Pageable pageable);
    java.util.List<ProductAdminDto> searchByQuery(String query);
    java.util.List<ProductAdminDto> findAllByIds(Iterable<String> ids);
    java.util.List<ProductAdminDto> findAllByFilterParameterValueId(String filterParameterValueId);
}
