package sk.tany.rest.api.dto.client.cart.add;

import lombok.Data;
import sk.tany.rest.api.dto.PriceBreakDown;

@Data
public class CartClientAddProductResponse {
    private String cartId;
    private PriceBreakDown priceBreakDown;
}
