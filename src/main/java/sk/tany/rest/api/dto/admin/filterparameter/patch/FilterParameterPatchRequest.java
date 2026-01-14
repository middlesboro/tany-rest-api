package sk.tany.rest.api.dto.admin.filterparameter.patch;

import lombok.Data;
import sk.tany.rest.api.domain.filter.FilterParameterType;

import java.util.List;

@Data
public class FilterParameterPatchRequest {
    private String name;
    private FilterParameterType type;
    private List<String> filterParameterValueIds;
    private Boolean active;
}
