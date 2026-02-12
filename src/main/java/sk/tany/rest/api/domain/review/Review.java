package sk.tany.rest.api.domain.review;

import lombok.Data;
import org.dizitart.no2.repository.annotations.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Review implements BaseEntity {

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

    @Override
    public void setCreatedDate(Instant date) {
        this.createDate = date;
    }
    @Override
    public Instant getCreatedDate() {
        return this.createDate;
    }
    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }

    @Override
    public Object getSortValue(String field) {
        if ("rating".equals(field)) {
            return rating;
        }
        return BaseEntity.super.getSortValue(field);
    }
}
