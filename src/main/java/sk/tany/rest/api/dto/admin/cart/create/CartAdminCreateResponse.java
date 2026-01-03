package sk.tany.rest.api.dto.admin.cart.create;

import lombok.Data;
import sk.tany.rest.api.dto.CartItem;

import java.time.Instant;
import java.util.List;

@Data
public class CartAdminCreateResponse {
    private String cartId;
    private String customerId;
    private Instant createDate;
    private Instant updateDate;
    private List<CartItem> items;
    private String selectedCarrierId;
    private String selectedPaymentId;
}
