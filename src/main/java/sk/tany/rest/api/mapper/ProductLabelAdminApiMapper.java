package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.ProductLabelDto;
import sk.tany.rest.api.dto.admin.productlabel.create.ProductLabelCreateRequest;
import sk.tany.rest.api.dto.admin.productlabel.create.ProductLabelCreateResponse;
import sk.tany.rest.api.dto.admin.productlabel.get.ProductLabelGetResponse;
import sk.tany.rest.api.dto.admin.productlabel.list.ProductLabelListResponse;
import sk.tany.rest.api.dto.admin.productlabel.update.ProductLabelUpdateRequest;
import sk.tany.rest.api.dto.admin.productlabel.update.ProductLabelUpdateResponse;

@Mapper(componentModel = "spring")
public interface ProductLabelAdminApiMapper {

    ProductLabelDto toDto(ProductLabelCreateRequest request);

    ProductLabelDto toDto(ProductLabelUpdateRequest request);

    ProductLabelCreateResponse toCreateResponse(ProductLabelDto dto);

    ProductLabelUpdateResponse toUpdateResponse(ProductLabelDto dto);

    ProductLabelGetResponse toGetResponse(ProductLabelDto dto);

    ProductLabelListResponse toListResponse(ProductLabelDto dto);
}
