package sk.tany.rest.api.domain.product;

import lombok.Data;

@Data
public class ProductFilterParameter {
    private String filterParameterId;
    private String filterParameterValueId;
}
