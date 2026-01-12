package sk.tany.rest.api.dto.client.review;

import lombok.Data;

@Data
public class ReviewClientCreateRequest {
    private String productId;
    private String text;
    private int rating;
    private String title;
    private String email;
}
