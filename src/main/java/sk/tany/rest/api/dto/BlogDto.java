package sk.tany.rest.api.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class BlogDto {
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
}
