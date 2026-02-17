package sk.tany.rest.api.dto.client.cart.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Email
    private String email;
    private String phone;
    @Valid
    private CartAddressDto invoiceAddress;
    @Valid
    private CartAddressDto deliveryAddress;
    private Boolean discountForNewsletter;
    private Instant createDate;
    private Instant updateDate;

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            this.email = null;
        } else {
            this.email = email;
        }
    }

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartAddressDto {
        private String street;
        private String city;
        private String zip;
        private String country;
    }
}
