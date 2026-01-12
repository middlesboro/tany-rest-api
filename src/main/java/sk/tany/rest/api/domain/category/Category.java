package sk.tany.rest.api.domain.category;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "categories")
public class Category {

    @Id
    private String id;
    private Long prestashopId;
    private Long prestashopParentId;
    private Long position;
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private boolean active;
    private boolean visible;
    private String slug;
    private String parentId;

}
