package sk.tany.rest.api.dto.client.cart.carrier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.dto.PriceBreakDown;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CartClientSetCarrierResponse {
    private String cartId;
    private String customerId;
    private Instant createDate;
    private Instant updateDate;
    private List<CartItem> items;
    private String selectedCarrierId;
    private String selectedPaymentId;
    private PriceBreakDown priceBreakDown;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        private String productId;
        private Integer quantity;
        private String title;
        private String image;
        private BigDecimal price;

        public CartItem(String productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
