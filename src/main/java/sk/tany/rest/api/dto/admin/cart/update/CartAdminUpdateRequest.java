package sk.tany.rest.api.dto.admin.cart.update;

import lombok.Data;
import sk.tany.rest.api.dto.CartItem;

import java.util.List;

@Data
public class CartAdminUpdateRequest {
    private String customerId;
    private List<CartItem> items;
    private String selectedCarrierId;
    private String selectedPaymentId;
}
