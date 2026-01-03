package sk.tany.rest.api.dto.client.cart.carrier;

import lombok.Data;

@Data
public class CartClientSetCarrierRequest {
    private String cartId;
    private String carrierId;
}
