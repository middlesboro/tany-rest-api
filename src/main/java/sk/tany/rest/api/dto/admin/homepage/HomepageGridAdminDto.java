package sk.tany.rest.api.dto.admin.homepage;

import lombok.Data;
import sk.tany.rest.api.domain.homepage.SortField;
import sk.tany.rest.api.domain.homepage.SortOrder;

import java.util.List;

@Data
public class HomepageGridAdminDto {
    private String id;
    private String title;
    private String brandId;
    private String categoryId;
    private List<String> productIds;
    private Integer resultCount;
    private SortField sortField;
    private SortOrder sortOrder;
}
