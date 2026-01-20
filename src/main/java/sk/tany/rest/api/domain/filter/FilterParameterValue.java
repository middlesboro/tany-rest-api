package sk.tany.rest.api.domain.filter;

import lombok.Data;

@Data
public class FilterParameterValue {

    private String id;
    private String name;
    private String filterParameterId;
    private Boolean active;
}
