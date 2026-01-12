package sk.tany.rest.api.dto.admin.review;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ReviewAdminCreateRequest {
    @NotNull
    private String productId;
    private String title;
    private String text;
    private String email;
    private int rating;
    private boolean active;
}
