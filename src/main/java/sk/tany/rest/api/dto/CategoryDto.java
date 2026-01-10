package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class CategoryDto {
    private String id;
    private Long prestashopId;
    private Long prestashopParentId;
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private boolean showInMenu;
    private boolean visible;
    private String slug;
    private String parentId;
}
