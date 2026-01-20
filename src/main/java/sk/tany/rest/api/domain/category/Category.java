package sk.tany.rest.api.domain.category;

import lombok.Data;
import java.time.Instant;

@Data
public class Category {

    private String id;
    private Long prestashopId;
    private Long prestashopParentId;
    private String title;
    private String description;
    private String image;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private String parentId;
    private int position;
    private boolean active;
    private boolean visible;
    private Instant createdDate;
    private Instant updateDate;
}
