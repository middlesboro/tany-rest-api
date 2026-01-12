package sk.tany.rest.api.domain.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewAggregationResult {
    private String id; // productId
    private double averageRating;
    private int reviewsCount;
}
