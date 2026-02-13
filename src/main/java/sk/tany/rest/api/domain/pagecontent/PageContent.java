package sk.tany.rest.api.domain.pagecontent;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class PageContent implements BaseEntity {

    @Id
    private String id;
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private String content;
    private boolean visible;
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
