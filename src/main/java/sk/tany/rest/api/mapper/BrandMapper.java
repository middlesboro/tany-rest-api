package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.dto.BrandDto;
import sk.tany.rest.api.dto.admin.brand.patch.BrandPatchRequest;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandDto toDto(Brand brand);
    Brand toEntity(BrandDto brandDto);
    void updateEntityFromDto(BrandDto brandDto, @MappingTarget Brand brand);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(BrandPatchRequest patch, @MappingTarget Brand brand);
}
