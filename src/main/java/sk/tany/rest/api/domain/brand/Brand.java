package sk.tany.rest.api.domain.brand;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Brand extends BaseEntity {
    private Long prestashopId;
    private String name;
    private String image;
    private String metaTitle;
    private String slug;
    private boolean active;
    private String metaDescription;
    @Override
    public Object getSortValue(String field) {
        switch (field) {
            case "name": return name;
            default: return super.getSortValue(field);
        }
    }
}
