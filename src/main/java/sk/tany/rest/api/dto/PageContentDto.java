package sk.tany.rest.api.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class PageContentDto {
    private String id;
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private boolean visible;
    private Instant createdDate;
    private Instant updateDate;
}
