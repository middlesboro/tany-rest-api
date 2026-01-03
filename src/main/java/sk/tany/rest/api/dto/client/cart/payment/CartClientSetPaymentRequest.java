package sk.tany.rest.api.dto.client.cart.payment;

import lombok.Data;

@Data
public class CartClientSetPaymentRequest {
    private String cartId;
    private String paymentId;
}
