package sk.tany.rest.api.service.client.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewAggregationResult;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.client.review.ReviewClientCreateRequest;
import sk.tany.rest.api.dto.client.review.ReviewClientProductResponse;
import sk.tany.rest.api.service.mapper.ReviewMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewClientServiceImplTest {

    @Mock
    private ReviewRepository repository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ReviewMapper mapper;
    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ReviewClientServiceImpl service;

    @Test
    void create_ShouldRecalculateProductStats() {
        // Given
        String productId = "prod1";
        ReviewClientCreateRequest request = new ReviewClientCreateRequest();
        request.setProductId(productId);
        request.setRating(5);

        ReviewAggregationResult aggResult = new ReviewAggregationResult(productId, 4.0, 2);
        AggregationResults<ReviewAggregationResult> aggResults = mock(AggregationResults.class);
        when(aggResults.getUniqueMappedResult()).thenReturn(aggResult);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("reviews"), eq(ReviewAggregationResult.class)))
                .thenReturn(aggResults);

        // When
        service.create(request);

        // Then
        verify(repository).save(any(Review.class));
        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(Product.class));
    }

    @Test
    void findAllByProductId_ShouldReturnStatsAndReviews() {
        // Given
        String productId = "prod1";
        Pageable pageable = Pageable.unpaged();
        Product product = new Product();
        product.setId(productId);
        // Note: product stats in entity are ignored in favor of aggregation
        product.setReviewsCount(0);
        product.setAverageRating(BigDecimal.ZERO);

        Page<Review> reviewPage = Page.empty();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(repository.findAllByProductId(productId, pageable)).thenReturn(reviewPage);

        // Mock aggregation result
        ReviewAggregationResult aggResult = new ReviewAggregationResult(productId, 4.5, 10);
        AggregationResults<ReviewAggregationResult> aggResults = mock(AggregationResults.class);
        when(aggResults.getUniqueMappedResult()).thenReturn(aggResult);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("reviews"), eq(ReviewAggregationResult.class)))
                .thenReturn(aggResults);

        // When
        ReviewClientProductResponse response = service.findAllByProductId(productId, pageable);

        // Then
        assertEquals(new BigDecimal("4.5"), response.getAverageRating());
        assertEquals(10, response.getReviewsCount());
        assertNotNull(response.getReviews());
    }
}
