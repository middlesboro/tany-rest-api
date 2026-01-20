package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.tany.rest.api.dto.admin.product.ProductAdminDto;
import sk.tany.rest.api.dto.admin.product.create.ProductCreateRequest;
import sk.tany.rest.api.dto.admin.product.create.ProductCreateResponse;
import sk.tany.rest.api.dto.admin.product.get.ProductGetResponse;
import sk.tany.rest.api.dto.admin.product.list.ProductListResponse;
import sk.tany.rest.api.dto.admin.product.search.ProductSearchResponse;
import sk.tany.rest.api.dto.admin.product.update.ProductUpdateRequest;
import sk.tany.rest.api.dto.admin.product.update.ProductUpdateResponse;
import sk.tany.rest.api.dto.admin.product.upload.ProductUploadImageResponse;

@Mapper(componentModel = "spring")
public interface ProductAdminApiMapper {
    @Mapping(target = "id", ignore = true)
    ProductAdminDto toDto(ProductCreateRequest request);
    ProductCreateResponse toCreateResponse(ProductAdminDto dto);

    ProductGetResponse toGetResponse(ProductAdminDto dto);

    ProductListResponse toListResponse(ProductAdminDto dto);

    ProductSearchResponse toSearchResponse(ProductAdminDto dto);

    @Mapping(target = "id", ignore = true)
    ProductAdminDto toDto(ProductUpdateRequest request);
    ProductUpdateResponse toUpdateResponse(ProductAdminDto dto);

    ProductUploadImageResponse toUploadImageResponse(ProductAdminDto dto);
}
