package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sk.tany.rest.api.domain.homepage.HomepageGrid;
import sk.tany.rest.api.dto.admin.homepage.HomepageGridAdminDto;

@Mapper(componentModel = "spring")
public interface HomepageGridMapper {
    HomepageGridAdminDto toAdminDto(HomepageGrid entity);
    HomepageGrid toEntity(HomepageGridAdminDto dto);
    void updateEntityFromDto(HomepageGridAdminDto dto, @MappingTarget HomepageGrid entity);
}
