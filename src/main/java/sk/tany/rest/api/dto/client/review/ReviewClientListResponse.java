package sk.tany.rest.api.dto.client.review;

import lombok.Data;

import java.time.Instant;

@Data
public class ReviewClientListResponse {
    private String id;
    private String title;
    private String text;
    private int rating;
    private String email;
    private String customerName;
    private Instant createDate;
}
