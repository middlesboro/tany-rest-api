package sk.tany.rest.api.dto.admin.category.patch;

import lombok.Data;
import sk.tany.rest.api.dto.FilterParameterDto;

import java.util.List;

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
    private List<FilterParameterDto> filterParameters;
    private List<FilterParameterDto> excludedFilterParameters;
}
