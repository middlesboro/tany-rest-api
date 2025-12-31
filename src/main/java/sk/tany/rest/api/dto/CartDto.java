package sk.tany.rest.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartDto {

    private String cartId;
    private String customerId;
    private List<String> productIds;
}
