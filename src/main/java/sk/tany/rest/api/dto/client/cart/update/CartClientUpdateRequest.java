package sk.tany.rest.api.dto.client.cart.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.dto.AddressDto;
import sk.tany.rest.api.validation.ValidEmail;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CartClientUpdateRequest {
    @NotBlank
    private String cartId;
    private String customerId;
    private String selectedCarrierId;
    private String selectedPaymentId;
    private String selectedPickupPointId;
    private String selectedPickupPointName;
    @Valid
    private List<CartItem> items;
    private String firstname;
    private String lastname;
    @ValidEmail
    private String email;
    private String phone;
    // We intentionally removed @Valid here to allow partial/empty updates (e.g. clearing an address field).
    // AddressDto has @NotBlank constraints which would fail if we validated empty fields.
    // The requirement is to allow empty values, effectively making address fields optional for this request.
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
    private Boolean discountForNewsletter;
    private Instant createDate;
    private Instant updateDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        @NotBlank
        private String productId;
        @NotNull
        @Min(1)
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
