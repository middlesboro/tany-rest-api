package sk.tany.rest.api.dto.client.customer.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.domain.carrier.CarrierType;
import sk.tany.rest.api.domain.customer.Role;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.domain.product.ProductStatus;
import sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CustomerClientGetResponse {
    private CustomerDto customerDto;
    private CustomerContextCartDto cartDto;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDto {
        private String id;
        private String firstname;
        private String lastname;
        private String email;
        private String phone;
        private Role role;
        private AddressDto invoiceAddress;
        private AddressDto deliveryAddress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private String street;
        private String city;
        private String zip;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerContextCartDto {
        private String cartId;
        private String customerId;
        private List<ProductDto> products;
        private BigDecimal totalProductPrice;
        private List<CarrierDto> carriers;
        private List<PaymentDto> payments;
        private String firstname;
        private String lastname;
        private String email;
        private String phone;
        private AddressDto deliveryAddress;
        private AddressDto invoiceAddress;
        private String selectedPickupPointId;
        private String selectedPickupPointName;
        private List<CartDiscountClientDto> appliedDiscounts;
        private BigDecimal totalPrice; // Products total
        private BigDecimal totalDiscount;
        private BigDecimal finalPrice; // Total - Discount + Carrier + Payment
        private boolean freeShipping;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDto {
        private String id;
        private String title;
        private String shortDescription;
        private String description;
        private BigDecimal wholesalePrice;
        private BigDecimal priceWithoutVat;
        private BigDecimal price;
        private BigDecimal weight;
        private Integer quantity;
        private String metaTitle;
        private String metaDescription;
        private String productCode;
        private String ean;
        private String slug;
        private List<String> categoryIds;
        private String supplierId;
        private String brandId;
        private List<String> images;
        private ProductStatus status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarrierDto {
        private String id;
        private String name;
        private String description;
        private String image;
        private Integer order;
        private CarrierType type;
        private BigDecimal price;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<CarrierPriceRangeDto> ranges;
        private Instant createdDate;
        private Instant updateDate;
        private boolean selected;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CarrierPriceRangeDto {
        private BigDecimal price;
        private BigDecimal weightFrom;
        private BigDecimal weightTo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDto {
        private String id;
        private String name;
        private String description;
        private String image;
        private BigDecimal price;
        private PaymentType type;
        private Instant createdDate;
        private Instant updateDate;
        private boolean selected;
    }
}
