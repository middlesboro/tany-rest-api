package sk.tany.rest.api.domain.blog;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Blog implements BaseEntity {

    @Id
    private String id;
    private String title;
    private String shortDescription;
    private String description;
    private String image;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private String author;
    private boolean visible;
    private int order;
    private Instant createdDate;
    private Instant updateDate;

    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
