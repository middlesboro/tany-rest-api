package sk.tany.rest.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CrossSellProductsResponse {

    private List<CrossSellProductDto> products;

}
