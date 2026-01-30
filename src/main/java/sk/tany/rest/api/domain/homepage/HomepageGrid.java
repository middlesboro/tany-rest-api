package sk.tany.rest.api.domain.homepage;

import lombok.Data;
import org.dizitart.no2.objects.Id;

import java.util.List;

@Data
public class HomepageGrid {
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
}
