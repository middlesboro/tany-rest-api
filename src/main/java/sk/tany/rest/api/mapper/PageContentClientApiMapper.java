package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.PageContentDto;
import sk.tany.rest.api.dto.client.pagecontent.get.PageContentClientGetResponse;

@Mapper(componentModel = "spring")
public interface PageContentClientApiMapper {
    PageContentClientGetResponse toGetResponse(PageContentDto dto);
}
