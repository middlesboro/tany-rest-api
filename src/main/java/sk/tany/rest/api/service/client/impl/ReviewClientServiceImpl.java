package sk.tany.rest.api.service.client.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewRepository;
import sk.tany.rest.api.dto.client.review.ProductRatingDto;
import sk.tany.rest.api.dto.client.review.ReviewClientCreateRequest;
import sk.tany.rest.api.dto.client.review.ReviewClientListResponse;
import sk.tany.rest.api.dto.client.review.ReviewClientProductResponse;
import sk.tany.rest.api.service.client.ReviewClientService;
import sk.tany.rest.api.service.mapper.ReviewMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewClientServiceImpl implements ReviewClientService {

    private final ReviewRepository repository;
    private final ProductRepository productRepository;
    private final ReviewMapper mapper;

    @Override
    public ReviewClientProductResponse findAllByProductId(String productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // Fetch all reviews and filter only active ones for client response
        List<Review> activeReviews = repository.findAllByProductId(productId, pageable.getSort()).stream()
                .filter(Review::isActive)
                .collect(Collectors.toList());

        BigDecimal averageRating = BigDecimal.ZERO;
        int reviewsCount = activeReviews.size();

        if (reviewsCount > 0) {
            double average = activeReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            averageRating = BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
        }

        List<ReviewClientListResponse> pageContent;

        if (pageable.isUnpaged()) {
            pageContent = activeReviews.stream()
                    .map(mapper::toClientListResponse)
                    .collect(Collectors.toList());
            Page<ReviewClientListResponse> reviews = new PageImpl<>(pageContent, pageable, activeReviews.size());
            return new ReviewClientProductResponse(
                averageRating,
                reviewsCount,
                reviews
            );
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), activeReviews.size());

        if (start > activeReviews.size()) {
            pageContent = List.of();
        } else {
            pageContent = activeReviews.subList(start, end).stream()
                    .map(mapper::toClientListResponse)
                    .collect(Collectors.toList());
        }

        Page<ReviewClientListResponse> reviews = new PageImpl<>(pageContent, pageable, activeReviews.size());

        return new ReviewClientProductResponse(
                averageRating,
                reviewsCount,
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

    @Override
    public ProductRatingDto getProductRating(String productId) {
        List<Review> activeReviews = repository.findAllByProductId(productId).stream()
                .filter(Review::isActive)
                .toList();

        BigDecimal averageRating = BigDecimal.ZERO;
        int reviewsCount = activeReviews.size();

        if (reviewsCount > 0) {
            double average = activeReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            averageRating = BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
        }
        return new ProductRatingDto(averageRating, reviewsCount);
    }

    @Override
    public Map<String, ProductRatingDto> getProductRatings(Collection<String> productIds) {
        List<Review> reviews = repository.findAllByProductIds(productIds);

        Map<String, List<Review>> reviewsByProduct = reviews.stream()
                .filter(Review::isActive)
                .collect(Collectors.groupingBy(Review::getProductId));

        Map<String, ProductRatingDto> result = new java.util.HashMap<>();

        for (String productId : productIds) {
            List<Review> productReviews = reviewsByProduct.getOrDefault(productId, List.of());

            BigDecimal averageRating = BigDecimal.ZERO;
            int reviewsCount = productReviews.size();

            if (reviewsCount > 0) {
                double average = productReviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);
                averageRating = BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
            }
            result.put(productId, new ProductRatingDto(averageRating, reviewsCount));
        }

        return result;
    }

    private void recalculateProductStats(String productId) {
        List<Review> reviews = repository.findAllByProductId(productId);
        List<Review> activeReviews = reviews.stream()
                .filter(Review::isActive)
                .toList();

        BigDecimal averageRating = BigDecimal.ZERO;
        int reviewsCount = activeReviews.size();

        if (reviewsCount > 0) {
            double average = activeReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            averageRating = BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setAverageRating(averageRating);
            product.setReviewsCount(reviewsCount);
            productRepository.save(product);
        }
    }

}
