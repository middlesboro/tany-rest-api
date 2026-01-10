package sk.tany.rest.api.domain.category;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "categories")
public class Category {

    @Id
    private String id;
    private long prestashopId;
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private boolean showInMenu;
    private boolean visible;
    private String slug;
    private String parentId;

}
