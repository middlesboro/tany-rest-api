package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.PageContentDto;
import sk.tany.rest.api.dto.admin.pagecontent.create.PageContentAdminCreateRequest;
import sk.tany.rest.api.dto.admin.pagecontent.create.PageContentAdminCreateResponse;
import sk.tany.rest.api.dto.admin.pagecontent.get.PageContentAdminGetResponse;
import sk.tany.rest.api.dto.admin.pagecontent.list.PageContentAdminListResponse;
import sk.tany.rest.api.dto.admin.pagecontent.update.PageContentAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.pagecontent.update.PageContentAdminUpdateResponse;

@Mapper(componentModel = "spring")
public interface PageContentAdminApiMapper {
    PageContentDto toDto(PageContentAdminCreateRequest request);
    PageContentDto toDto(PageContentAdminUpdateRequest request);

    PageContentAdminCreateResponse toCreateResponse(PageContentDto dto);
    PageContentAdminUpdateResponse toUpdateResponse(PageContentDto dto);
    PageContentAdminGetResponse toGetResponse(PageContentDto dto);
    PageContentAdminListResponse toListResponse(PageContentDto dto);
}
