package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class CartCarrierRequest {
    private String cartId;
    private String carrierId;
}
