package sk.tany.rest.api.dto.client.blog.get;

import lombok.Data;
import java.time.Instant;

@Data
public class BlogClientGetResponse {
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
    private Instant createdDate;
    private Instant updateDate;
}
