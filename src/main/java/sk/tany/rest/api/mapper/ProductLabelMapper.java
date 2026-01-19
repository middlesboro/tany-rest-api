package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.domain.productlabel.ProductLabel;
import sk.tany.rest.api.dto.ProductLabelDto;

@Mapper(componentModel = "spring")
public interface ProductLabelMapper {
    ProductLabelDto toDto(ProductLabel productLabel);
    ProductLabel toEntity(ProductLabelDto productLabelDto);
}
