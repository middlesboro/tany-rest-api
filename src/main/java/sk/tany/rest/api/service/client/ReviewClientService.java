package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.client.review.ReviewClientCreateRequest;
import sk.tany.rest.api.dto.client.review.ReviewClientProductResponse;

public interface ReviewClientService {
    ReviewClientProductResponse findAllByProductId(String productId, Pageable pageable);

    void create(ReviewClientCreateRequest request);
}
