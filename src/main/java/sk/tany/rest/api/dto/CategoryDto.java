package sk.tany.rest.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryDto {
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
    private List<CategoryDto> children;
    private List<FilterParameterDto> filterParameters;
}
