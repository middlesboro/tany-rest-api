package sk.tany.rest.api.dto.client.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRatingDto {
    private BigDecimal averageRating;
    private int reviewsCount;
}
