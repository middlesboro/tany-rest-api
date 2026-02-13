package sk.tany.rest.api.domain.brand;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Brand implements BaseEntity {
    @Id
    private String id;
    private Long prestashopId;
    private String name;
    private String image;
    private String metaTitle;
    private String slug;
    private boolean active;
    private String metaDescription;
    private Instant createdDate;
    private Instant updateDate;

    @Override
    public Object getSortValue(String field) {
        switch (field) {
            case "name": return name;
            default: return BaseEntity.super.getSortValue(field);
        }
    }

    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
