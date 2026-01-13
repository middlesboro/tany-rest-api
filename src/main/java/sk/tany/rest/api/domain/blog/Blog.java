package sk.tany.rest.api.domain.blog;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "blogs")
public class Blog {
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
    @CreatedDate
    private Instant createdDate;
    @LastModifiedDate
    private Instant updateDate;
}
