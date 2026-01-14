package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.filter.FilterParameter;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.admin.filterparameter.patch.FilterParameterPatchRequest;

@Mapper(componentModel = "spring")
public interface FilterParameterMapper {
    FilterParameterDto toDto(FilterParameter entity);
    FilterParameter toEntity(FilterParameterDto dto);
    void updateEntityFromDto(FilterParameterDto dto, @MappingTarget FilterParameter entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(FilterParameterPatchRequest patch, @MappingTarget FilterParameter entity);
}
