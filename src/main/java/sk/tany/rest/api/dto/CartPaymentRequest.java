package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class CartPaymentRequest {
    private String cartId;
    private String paymentId;
}
