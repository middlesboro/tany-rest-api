package sk.tany.rest.api.dto.client.cart.update;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.validation.CartClientUpdateConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@CartClientUpdateConstraint
@Data
public class CartClientUpdateRequest {
    private String cartId;
    private String customerId;
    private String selectedCarrierId;
    private String selectedPaymentId;
    private String selectedPickupPointId;
    private String selectedPickupPointName;
    private List<CartItem> items;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    @Valid
    private AddressDto invoiceAddress;
    @Valid
    private AddressDto deliveryAddress;
    private Boolean discountForNewsletter;
    private Instant createDate;
    private Instant updateDate;

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
