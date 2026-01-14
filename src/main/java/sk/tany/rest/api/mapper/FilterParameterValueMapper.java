package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.filter.FilterParameterValue;
import sk.tany.rest.api.dto.FilterParameterValueDto;
import sk.tany.rest.api.dto.admin.filterparametervalue.patch.FilterParameterValuePatchRequest;

@Mapper(componentModel = "spring")
public interface FilterParameterValueMapper {
    FilterParameterValueDto toDto(FilterParameterValue entity);
    FilterParameterValue toEntity(FilterParameterValueDto dto);
    void updateEntityFromDto(FilterParameterValueDto dto, @MappingTarget FilterParameterValue entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(FilterParameterValuePatchRequest patch, @MappingTarget FilterParameterValue entity);
}
