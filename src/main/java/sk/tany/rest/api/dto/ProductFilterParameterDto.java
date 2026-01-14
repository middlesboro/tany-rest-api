package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class ProductFilterParameterDto {
    private String filterParameterId;
    private String filterParameterValueId;
}
