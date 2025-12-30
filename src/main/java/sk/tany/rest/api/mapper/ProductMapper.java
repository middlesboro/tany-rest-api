package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.dto.ProductDto;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);
    Product toEntity(ProductDto productDto);
}
