package sk.tany.rest.api.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.admin.product.filter.ProductFilter;

public interface ProductRepositoryCustom {
    Page<Product> findAll(ProductFilter filter, Pageable pageable);
}
