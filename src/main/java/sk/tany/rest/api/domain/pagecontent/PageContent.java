package sk.tany.rest.api.domain.pagecontent;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "page_contents")
public class PageContent {
    @Id
    private String id;
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private boolean visible;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedDate
    private Instant updateDate;
}
