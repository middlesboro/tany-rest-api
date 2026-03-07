package sk.tany.rest.api.dto.client.cart.remove;

import lombok.Data;
import sk.tany.rest.api.validation.client.cart.CartClientRemoveItemConstraint;

@Data
@CartClientRemoveItemConstraint
public class CartClientRemoveItemRequest {
    private String cartId;
    private String productId;
}
