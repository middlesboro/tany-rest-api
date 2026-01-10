package sk.tany.rest.api.dto.admin.cart.patch;

import lombok.Data;
import sk.tany.rest.api.dto.CartItem;

import java.util.List;

@Data
public class CartPatchRequest {
    private String customerId;
    private List<CartItem> items;
    private String selectedCarrierId;
    private String selectedPaymentId;
}
