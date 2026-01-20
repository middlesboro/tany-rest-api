package sk.tany.rest.api.domain.filter;

import lombok.Data;

import java.util.List;

@Data
public class FilterParameter {

    private String id;
    private String name;
    private FilterParameterType type;
    private List<String> filterParameterValueIds;
    private Boolean active;
}
