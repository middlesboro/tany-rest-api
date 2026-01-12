package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.admin.review.ReviewAdminCreateRequest;
import sk.tany.rest.api.dto.admin.review.ReviewAdminDetailResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminListResponse;
import sk.tany.rest.api.dto.admin.review.ReviewAdminUpdateRequest;

public interface ReviewAdminService {
    Page<ReviewAdminListResponse> findAll(Pageable pageable);

    ReviewAdminDetailResponse findById(String id);

    ReviewAdminDetailResponse create(ReviewAdminCreateRequest request);

    ReviewAdminDetailResponse update(String id, ReviewAdminUpdateRequest request);

    void delete(String id);

    void importReviews();
}
