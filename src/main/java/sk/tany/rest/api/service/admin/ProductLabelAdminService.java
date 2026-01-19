package sk.tany.rest.api.service.admin;

import sk.tany.rest.api.dto.ProductLabelDto;

import java.util.List;
import java.util.Optional;

public interface ProductLabelAdminService {
    ProductLabelDto save(ProductLabelDto productLabelDto);
    ProductLabelDto update(String id, ProductLabelDto productLabelDto);
    void deleteById(String id);
    Optional<ProductLabelDto> findById(String id);
    List<ProductLabelDto> findAll();
    List<ProductLabelDto> findAllByProductId(String productId);
}
