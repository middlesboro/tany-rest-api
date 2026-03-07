package sk.tany.rest.api.dto.client.cart.add;

import lombok.Data;
import sk.tany.rest.api.validation.client.cart.CartClientAddItemConstraint;

@Data
@CartClientAddItemConstraint
public class CartClientAddItemRequest {
    private String cartId;
    private String productId;
    private int quantity;
}
