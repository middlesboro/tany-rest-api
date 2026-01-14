package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class FilterParameterValueDto {
    private String id;
    private String name;
    private Boolean active;
}
