package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class ClientFilterParameterValueDto {
    private String id;
    private String filterParameterId;
    private String name;
    private Boolean active;
    private Boolean selected;
    private Boolean available;
}
