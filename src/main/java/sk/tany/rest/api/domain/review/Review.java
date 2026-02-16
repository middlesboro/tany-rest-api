package sk.tany.rest.api.domain.review;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Review extends BaseEntity {
private String productId;
    private Long prestashopProductId;
    private String text;
    private Integer rating;
    private String title;
    private String email;
    private String customerId;
    private String customerName;
    private boolean active;
@Override
    public Object getSortValue(String field) {
        if ("rating".equals(field)) {
            return rating;
        }
        return super.getSortValue(field);
    }
}
