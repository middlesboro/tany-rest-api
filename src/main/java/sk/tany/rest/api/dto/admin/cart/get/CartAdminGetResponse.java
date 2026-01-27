package sk.tany.rest.api.dto.admin.cart.get;

import lombok.Data;
import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.PriceBreakDown;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CartAdminGetResponse {
    private String cartId;
    private String customerId;
    private Instant createDate;
    private Instant updateDate;
    private List<CartItem> items;
    private String selectedCarrierId;
    private String selectedPaymentId;
    private PriceBreakDown priceBreakDown;
    private String customerName;
    private Long orderIdentifier;
    private BigDecimal price;
    private String carrierName;
    private String paymentName;
}
