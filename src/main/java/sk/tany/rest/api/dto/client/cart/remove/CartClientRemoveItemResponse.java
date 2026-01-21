package sk.tany.rest.api.dto.client.cart.remove;

import lombok.Data;
import sk.tany.rest.api.dto.PriceBreakDown;

@Data
public class CartClientRemoveItemResponse {
    private String cartId;
    private PriceBreakDown priceBreakDown;
}
