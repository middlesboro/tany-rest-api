package sk.tany.rest.api.dto.admin.category.patch;

import lombok.Data;

@Data
public class CategoryPatchRequest {
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private Boolean showInMenu;
    private Boolean visible;
    private String slug;
    private String parentId;
}
