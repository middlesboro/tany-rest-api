package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.homepage.HomepageGrid;
import sk.tany.rest.api.dto.admin.homepage.HomepageGridAdminDto;
import sk.tany.rest.api.dto.admin.homepage.patch.HomepageGridPatchRequest;

@Mapper(componentModel = "spring")
public interface HomepageGridMapper {
    HomepageGridAdminDto toAdminDto(HomepageGrid entity);
    HomepageGrid toEntity(HomepageGridAdminDto dto);
    void updateEntityFromDto(HomepageGridAdminDto dto, @MappingTarget HomepageGrid entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(HomepageGridPatchRequest patch, @MappingTarget HomepageGrid entity);
}
