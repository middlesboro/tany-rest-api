package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sk.tany.rest.api.domain.contentsnippet.ContentSnippet;
import sk.tany.rest.api.dto.ContentSnippetDto;

@Mapper(componentModel = "spring")
public interface ContentSnippetMapper {
    ContentSnippetDto toDto(ContentSnippet entity);
    ContentSnippet toEntity(ContentSnippetDto dto);
    void updateEntity(ContentSnippetDto dto, @MappingTarget ContentSnippet entity);
}
