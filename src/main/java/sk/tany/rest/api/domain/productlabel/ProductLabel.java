package sk.tany.rest.api.domain.productlabel;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class ProductLabel implements BaseEntity {
    @Id
    private String id;
    private String title;
    private String color;
    private String backgroundColor;
    private ProductLabelPosition position;
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
}
