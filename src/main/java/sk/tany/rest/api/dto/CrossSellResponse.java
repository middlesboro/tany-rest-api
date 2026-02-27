package sk.tany.rest.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CrossSellResponse {
    private String sourceProductId;
    private List<CrossSellProductDto> crossSellProducts;
}
