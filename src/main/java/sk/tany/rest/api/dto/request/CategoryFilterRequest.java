package sk.tany.rest.api.dto.request;

import lombok.Data;

import java.util.List;
import sk.tany.rest.api.validation.client.product.CategoryFilterConstraint;

@Data
@CategoryFilterConstraint
public class CategoryFilterRequest {
    private List<FilterParameterRequest> filterParameters;
    private SortOption sort;
}
