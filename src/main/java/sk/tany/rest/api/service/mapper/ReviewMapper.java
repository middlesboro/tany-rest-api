package sk.tany.rest.api.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.dto.admin.review.ReviewAdminCreateRequest;
import sk.tany.rest.api.dto.admin.review.ReviewAdminDetailResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminListResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminUpdateRequest;
import sk.tany.rest.api.dto.client.review.ReviewClientListResponse;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewAdminListResponse toAdminListResponse(Review entity);

    ReviewAdminDetailResponse toAdminDetailResponse(Review entity);

    Review toEntity(ReviewAdminCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ReviewAdminUpdateRequest request, @MappingTarget Review entity);

    ReviewClientListResponse toClientListResponse(Review entity);
}
