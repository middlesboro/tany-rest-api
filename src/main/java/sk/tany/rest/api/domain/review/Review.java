package sk.tany.rest.api.domain.review;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import java.time.Instant;

@Data
public class Review {

    @Id
    private String id;
    private String productId;
    private Long prestashopProductId;
    private String text;
    private Integer rating;
    private String title;
    private String email;
    private String customerId;
    private String customerName;
    private boolean active;
    private Instant createDate;
    private Instant updateDate;
}
