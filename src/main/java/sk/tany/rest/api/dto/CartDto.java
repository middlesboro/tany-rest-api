package sk.tany.rest.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CartDto {

    private String cartId;
    private String customerId;
    private Map<String, Integer> products;
}
