package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.dto.admin.product.patch.ProductPatchRequest;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);
    Product toEntity(ProductDto productDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(ProductPatchRequest patch, @MappingTarget Product product);
}
