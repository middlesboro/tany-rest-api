package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.dto.admin.brand.BrandAdminGetResponse;
import sk.tany.rest.api.dto.admin.brand.patch.BrandPatchRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandDto toDto(Brand brand);

    @Mapping(target = "productIds", source = "productIds")
    BrandAdminGetResponse toAdminDetailDto(Brand brand, List<String> productIds);

    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Brand toEntity(BrandDto brandDto);

    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    void updateEntityFromDto(BrandDto brandDto, @MappingTarget Brand brand);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    void updateEntityFromPatch(BrandPatchRequest patch, @MappingTarget Brand brand);
}
