package sk.tany.rest.api.dto.client.review;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewClientCreateRequest {
    @NotBlank
    private String productId;
    @NotBlank
    private String text;
    @Min(1)
    @Max(5)
    private int rating;
    @NotBlank
    private String title;
    @NotBlank
    @Email
    private String email;
}
