package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.ContentSnippetDto;
import sk.tany.rest.api.dto.admin.contentsnippet.create.ContentSnippetAdminCreateRequest;
import sk.tany.rest.api.dto.admin.contentsnippet.create.ContentSnippetAdminCreateResponse;
import sk.tany.rest.api.dto.admin.contentsnippet.get.ContentSnippetAdminGetResponse;
import sk.tany.rest.api.dto.admin.contentsnippet.list.ContentSnippetAdminListResponse;
import sk.tany.rest.api.dto.admin.contentsnippet.update.ContentSnippetAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.contentsnippet.update.ContentSnippetAdminUpdateResponse;

@Mapper(componentModel = "spring")
public interface ContentSnippetAdminApiMapper {
    ContentSnippetDto toDto(ContentSnippetAdminCreateRequest request);
    ContentSnippetDto toDto(ContentSnippetAdminUpdateRequest request);
    ContentSnippetAdminCreateResponse toCreateResponse(ContentSnippetDto dto);
    ContentSnippetAdminUpdateResponse toUpdateResponse(ContentSnippetDto dto);
    ContentSnippetAdminGetResponse toGetResponse(ContentSnippetDto dto);
    ContentSnippetAdminListResponse toListResponse(ContentSnippetDto dto);
}
