package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.BlogDto;
import sk.tany.rest.api.dto.client.blog.get.BlogClientGetResponse;

@Mapper(componentModel = "spring")
public interface BlogClientApiMapper {
    BlogClientGetResponse toGetResponse(BlogDto dto);
}
