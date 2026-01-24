package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.client.review.ProductRatingDto;
import sk.tany.rest.api.dto.client.review.ReviewClientCreateRequest;
import sk.tany.rest.api.dto.client.review.ReviewClientProductResponse;

import java.util.Collection;
import java.util.Map;

public interface ReviewClientService {
    ReviewClientProductResponse findAllByProductId(String productId, Pageable pageable);

    void create(ReviewClientCreateRequest request);

    ProductRatingDto getProductRating(String productId);

    Map<String, ProductRatingDto> getProductRatings(Collection<String> productIds);
}
