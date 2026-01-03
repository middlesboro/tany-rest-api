package sk.tany.rest.api.dto.client.cart.add;

import lombok.Data;

@Data
public class CartClientAddItemRequest {
    private String cartId;
    private String productId;
    private int quantity;
}
