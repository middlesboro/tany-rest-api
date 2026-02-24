package sk.tany.rest.api.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.review.Review;
import sk.tany.rest.api.domain.review.ReviewRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewProductRatingEventListener extends AbstractMongoEventListener<Review> {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public void onAfterSave(AfterSaveEvent<Review> event) {
        Review review = event.getSource();
        if (review.getProductId() != null) {
            recalculateProductStats(review.getProductId());
        }
    }

    // @Override
    // public void onAfterDelete(AfterDeleteEvent<Review> event) {
        // Implementation for hard deletes is complex due to missing entity data.
        // For now, we rely on the fact that most changes are creates/updates (active=false).
    // }

    private void recalculateProductStats(String productId) {
        List<Review> activeReviews = reviewRepository.findAllByProductId(productId).stream()
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
            // Only update if changed to avoid infinite loops if product save triggers something else (though unlikely here)
            // But we must save the product to update the fields.
            // Saving the product will trigger ProductEmbeddingEventListener, which is what we want!
            product.setAverageRating(averageRating);
            product.setReviewsCount(reviewsCount);
            productRepository.save(product);
            log.debug("Recalculated rating for product {}: {} stars, {} reviews", productId, averageRating, reviewsCount);
        }
    }
}
