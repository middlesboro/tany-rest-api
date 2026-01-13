package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.pagecontent.PageContent;
import sk.tany.rest.api.dto.PageContentDto;

@Mapper(componentModel = "spring")
public interface PageContentMapper {
    PageContentDto toDto(PageContent entity);

    PageContent toEntity(PageContentDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(PageContentDto dto, @MappingTarget PageContent entity);
}
