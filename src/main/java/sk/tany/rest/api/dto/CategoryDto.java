package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class CategoryDto {
    private String id;
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private String parentId;
}
