package sk.tany.rest.api.dto;

import lombok.Data;
import sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto;
import sk.tany.rest.api.dto.client.product.ProductClientDto;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomerContextCartDto {

    private String cartId;
    private String customerId;
    private List<ProductClientDto> products;
    private java.math.BigDecimal totalProductPrice;
    private List<CarrierDto> carriers;
    private List<PaymentDto> payments;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String selectedPickupPointId;
    private String selectedPickupPointName;
    private List<CartDiscountClientDto> appliedDiscounts;
    private BigDecimal totalPrice; // Products total
    private BigDecimal totalDiscount;
    private BigDecimal finalPrice; // Total - Discount + Carrier + Payment
    private boolean freeShipping;

    // Discount related fields
    private List<CartDiscountClientDto> appliedDiscounts;
    private java.math.BigDecimal totalDiscount;
    private java.math.BigDecimal finalPrice; // Total - Discount + Carrier + Payment
    private boolean freeShipping;

}
