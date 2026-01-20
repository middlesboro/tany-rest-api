package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.dto.ProductLabelDto;
import sk.tany.rest.api.dto.client.product.get.ProductClientGetResponse;
import sk.tany.rest.api.dto.client.product.label.ProductLabelClientDto;
import sk.tany.rest.api.dto.client.product.list.ProductClientListResponse;

@Mapper(componentModel = "spring")
public interface ProductClientApiMapper {
    ProductClientGetResponse toGetResponse(ProductClientDto dto);
    ProductClientListResponse toListResponse(ProductClientDto dto);
    ProductLabelClientDto toLabelClientDto(ProductLabelDto dto);
}
