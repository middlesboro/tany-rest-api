package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sk.tany.rest.api.domain.brand.Brand;
import sk.tany.rest.api.dto.BrandDto;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandDto toDto(Brand brand);
    Brand toEntity(BrandDto brandDto);
    void updateEntityFromDto(BrandDto brandDto, @MappingTarget Brand brand);
}
