package sk.tany.rest.api.domain.homepage;

import lombok.Data;
import org.dizitart.no2.repository.annotations.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;
import java.util.List;

@Data
public class HomepageGrid implements BaseEntity {
    @Id
    private String id;
    private String title;
    private String brandId;
    private String categoryId;
    private List<String> productIds;
    private Integer resultCount;
    private Integer order;
    private SortField sortField;
    private SortOrder sortOrder;

    @Override
    public void setCreatedDate(Instant date) { }
    @Override
    public Instant getCreatedDate() { return null; }
    @Override
    public void setLastModifiedDate(Instant date) { }
    @Override
    public Instant getLastModifiedDate() { return null; }

    @Override
    public Object getSortValue(String field) {
        if ("order".equals(field)) {
            return order;
        }
        return BaseEntity.super.getSortValue(field);
    }
}
