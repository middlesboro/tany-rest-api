package sk.tany.rest.api.service.client.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.client.review.ReviewClientListResponse;
import sk.tany.rest.api.dto.client.review.ReviewClientProductResponse;
import sk.tany.rest.api.service.mapper.ReviewMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewClientServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewClientServiceImpl service;

    private Product product;
    private Review review1;
    private Review review2;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId("product1");
        // Product stats are stale/empty
        product.setAverageRating(BigDecimal.ZERO);
        product.setReviewsCount(0);

        review1 = new Review();
        review1.setProductId("product1");
        review1.setRating(5);
        review1.setActive(true);

        review2 = new Review();
        review2.setProductId("product1");
        review2.setRating(3);
        review2.setActive(true);
    }

    @Test
    void findAllByProductId_shouldRecalculateStatsOnTheFly() {
        when(productRepository.findById("product1")).thenReturn(Optional.of(product));
        when(reviewRepository.findAllByProductId(eq("product1"), any(Sort.class))).thenReturn(List.of(review1, review2));
        when(reviewMapper.toClientListResponse(any())).thenReturn(new ReviewClientListResponse());

        ReviewClientProductResponse response = service.findAllByProductId("product1", Pageable.unpaged());

        // We expect stats to be calculated from reviews, not taken from product
        // Average of 5 and 3 is 4.0
        // Count is 2
        assertEquals(BigDecimal.valueOf(4.0), response.getAverageRating());
        assertEquals(2, response.getReviewsCount());
    }

    @Test
    void findAllByProductId_shouldPassSortToRepository() {
        when(productRepository.findById("product1")).thenReturn(Optional.of(product));
        Sort sort = Sort.by("rating").descending();
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10, sort);

        when(reviewRepository.findAllByProductId("product1", sort)).thenReturn(List.of(review1, review2));
        when(reviewMapper.toClientListResponse(any())).thenReturn(new ReviewClientListResponse());

        service.findAllByProductId("product1", pageable);

        verify(reviewRepository).findAllByProductId("product1", sort);
    }
}
