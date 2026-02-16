package sk.tany.rest.api.domain.homepage;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;
import java.util.List;

@Data
public class HomepageGrid extends BaseEntity {
private String title;
    private String brandId;
    private String categoryId;
    private List<String> productIds;
    private Integer resultCount;
    private Integer order;
    private SortField sortField;
    private SortOrder sortOrder;
@Override
    public Object getSortValue(String field) {
        if ("order".equals(field)) {
            return order;
        }
        return super.getSortValue(field);
    }
}
