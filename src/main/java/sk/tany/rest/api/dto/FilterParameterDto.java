package sk.tany.rest.api.dto;

import lombok.Data;
import sk.tany.rest.api.domain.filter.FilterParameterType;

import java.util.List;

@Data
public class FilterParameterDto {
    private String id;
    private String name;
    private FilterParameterType type;
    private List<String> filterParameterValueIds;
    private List<FilterParameterValueDto> values;
    private Boolean active;
}
