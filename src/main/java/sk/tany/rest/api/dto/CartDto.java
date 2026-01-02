package sk.tany.rest.api.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CartDto {

    private String cartId;
    private String customerId;
    private String selectedCarrierId;
    private String selectedPaymentId;
    private List<CartItem> items;
    private Instant createDate;
    private Instant updateDate;
}
