package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewAggregationResult;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.client.review.ReviewClientCreateRequest;
import sk.tany.rest.api.dto.client.review.ReviewClientListResponse;
import sk.tany.rest.api.dto.client.review.ReviewClientProductResponse;
import sk.tany.rest.api.service.client.ReviewClientService;
import sk.tany.rest.api.service.mapper.ReviewMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewClientServiceImpl implements ReviewClientService {

    private final ReviewRepository repository;
    private final ProductRepository productRepository;
    private final ReviewMapper mapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public ReviewClientProductResponse findAllByProductId(String productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Page<ReviewClientListResponse> reviews = repository.findAllByProductId(productId, pageable)
                .map(mapper::toClientListResponse);

        ReviewStats stats = getReviewStats(productId);

        return new ReviewClientProductResponse(
                stats.averageRating,
                stats.reviewsCount,
                reviews
        );
    }

    @Override
    public void create(ReviewClientCreateRequest request) {
        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setText(request.getText());
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setEmail(request.getEmail());
        review.setActive(true);
        repository.save(review);
        recalculateProductStats(request.getProductId());
    }

    private void recalculateProductStats(String productId) {
        ReviewStats stats = getReviewStats(productId);

        Update update = new Update();
        update.set("averageRating", stats.averageRating);
        update.set("reviewsCount", stats.reviewsCount);

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(productId)),
                update,
                Product.class
        );
    }

    private ReviewStats getReviewStats(String productId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("productId").is(productId).and("active").is(true)),
                Aggregation.group("productId")
                        .avg("rating").as("averageRating")
                        .count().as("reviewsCount")
        );

        AggregationResults<ReviewAggregationResult> results = mongoTemplate.aggregate(
                aggregation, "reviews", ReviewAggregationResult.class);

        ReviewAggregationResult result = results.getUniqueMappedResult();

        BigDecimal averageRating = BigDecimal.ZERO;
        int reviewsCount = 0;

        if (result != null) {
            averageRating = BigDecimal.valueOf(result.getAverageRating()).setScale(1, RoundingMode.HALF_UP);
            reviewsCount = result.getReviewsCount();
        }
        return new ReviewStats(averageRating, reviewsCount);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ReviewStats {
        private BigDecimal averageRating;
        private int reviewsCount;
    }
}
