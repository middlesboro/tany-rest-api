package sk.tany.rest.api.domain.category;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;
import sk.tany.rest.api.dto.FilterParameterDto;

import java.time.Instant;
import java.util.List;

@Data
public class Category extends BaseEntity {
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
    private boolean defaultCategory;
    private List<FilterParameterDto> filterParameters;
    private List<FilterParameterDto> excludedFilterParameters;
    @Override
    public Object getSortValue(String field) {
        switch (field) {
            case "position": return position;
            case "title": return title;
            case "visible": return visible;
            default: return super.getSortValue(field);
        }
    }
}
