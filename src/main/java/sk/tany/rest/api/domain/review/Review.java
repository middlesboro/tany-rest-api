package sk.tany.rest.api.domain.review;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    @CreatedDate
    private Instant createDate;
    @LastModifiedDate
    private Instant updateDate;
    private String productId;
    private Long prestashopProductId;
    private String text;
    private int rating;
    private String title;
    private String customerName;
    private String email;
    private boolean active;
}
