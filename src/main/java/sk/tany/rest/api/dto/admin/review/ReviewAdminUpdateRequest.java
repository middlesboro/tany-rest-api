package sk.tany.rest.api.dto.admin.review;

import lombok.Data;

@Data
public class ReviewAdminUpdateRequest {
    private String title;
    private String text;
    private String email;
    private Integer rating;
    private Boolean active;
}
