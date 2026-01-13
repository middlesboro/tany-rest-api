package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.blog.Blog;
import sk.tany.rest.api.dto.BlogDto;

@Mapper(componentModel = "spring")
public interface BlogMapper {
    BlogDto toDto(Blog entity);
    Blog toEntity(BlogDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(BlogDto dto, @MappingTarget Blog entity);
}
