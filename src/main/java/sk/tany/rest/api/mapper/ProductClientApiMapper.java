package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.ProductDto;
import sk.tany.rest.api.dto.ProductLabelDto;
import sk.tany.rest.api.dto.client.product.get.ProductClientGetResponse;
import sk.tany.rest.api.dto.client.product.label.ProductLabelClientDto;
import sk.tany.rest.api.dto.client.product.list.ProductClientListResponse;

@Mapper(componentModel = "spring")
public interface ProductClientApiMapper {
    ProductClientGetResponse toGetResponse(ProductDto dto);
    ProductClientListResponse toListResponse(ProductDto dto);
    ProductLabelClientDto toLabelClientDto(ProductLabelDto dto);
}
