package sk.tany.rest.api.domain.category;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import sk.tany.rest.api.domain.BaseEntity;
import sk.tany.rest.api.dto.FilterParameterDto;

import java.time.Instant;
import java.util.List;

@Data
public class Category implements BaseEntity {

    @Id
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
    private boolean defaultCategory;
    private List<FilterParameterDto> filterParameters;
    private Instant createdDate;
    private Instant updateDate;

    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }

    @Override
    public Object getSortValue(String field) {
        switch (field) {
            case "position": return position;
            case "title": return title;
            case "visible": return visible;
            default: return BaseEntity.super.getSortValue(field);
        }
    }
}
