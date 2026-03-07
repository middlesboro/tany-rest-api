package sk.tany.rest.api.dto.client.cart.payment;

import lombok.Data;
import sk.tany.rest.api.validation.client.cart.CartClientSetPaymentConstraint;

@Data
@CartClientSetPaymentConstraint
public class CartClientSetPaymentRequest {
    private String cartId;
    private String paymentId;
}
