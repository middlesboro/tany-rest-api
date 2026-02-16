package sk.tany.rest.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CategoryDto {
    private String id;
    private Long prestashopId;
    private Long prestashopParentId;
    private Long position;
    @NotBlank
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private boolean active;
    private boolean visible;
    private boolean defaultCategory;
    @NotBlank
    private String slug;
    private String parentId;
    private List<CategoryDto> children;
    private List<FilterParameterDto> filterParameters;
}
