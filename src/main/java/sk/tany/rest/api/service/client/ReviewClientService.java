package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.client.review.ReviewClientListResponse;

public interface ReviewClientService {
    Page<ReviewClientListResponse> findAllByProductId(String productId, Pageable pageable);
}
