package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private String cartId;
    private String productId;
}
