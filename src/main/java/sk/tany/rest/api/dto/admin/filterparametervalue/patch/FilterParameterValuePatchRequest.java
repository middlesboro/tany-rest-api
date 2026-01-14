package sk.tany.rest.api.dto.admin.filterparametervalue.patch;

import lombok.Data;

@Data
public class FilterParameterValuePatchRequest {
    private String name;
    private Boolean active;
}
