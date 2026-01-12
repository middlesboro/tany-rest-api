package sk.tany.rest.api.dto.client.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewClientProductResponse {
    private BigDecimal averageRating;
    private Integer reviewsCount;
    private Page<ReviewClientListResponse> reviews;
}
