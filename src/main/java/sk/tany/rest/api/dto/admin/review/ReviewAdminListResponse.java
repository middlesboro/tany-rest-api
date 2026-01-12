package sk.tany.rest.api.dto.admin.review;

import lombok.Data;
import java.time.Instant;

@Data
public class ReviewAdminListResponse {
    private String id;
    private String productId;
    private String title;
    private String email;
    private int rating;
    private boolean active;
    private Instant createDate;
}
