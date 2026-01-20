package sk.tany.rest.api.domain.review;

import lombok.Data;
import java.time.Instant;

@Data
public class Review {

    private String id;
    private String productId;
    private Long prestashopProductId; // Added
    private String text;
    private Integer rating;
    private String title;
    private String email;
    private String customerName; // Added
    private boolean active;
    private Instant createDate;
    private Instant updateDate;
}
