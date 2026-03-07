package sk.tany.rest.api.dto.client.cart.carrier;

import lombok.Data;
import sk.tany.rest.api.validation.client.cart.CartClientSetCarrierConstraint;

@Data
@CartClientSetCarrierConstraint
public class CartClientSetCarrierRequest {
    private String cartId;
    private String carrierId;
}
