package sk.tany.rest.api.dto.client.cart.remove;

import lombok.Data;

@Data
public class CartClientRemoveItemRequest {
    private String cartId;
    private String productId;
}
