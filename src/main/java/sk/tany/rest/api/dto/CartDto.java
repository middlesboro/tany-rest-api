package sk.tany.rest.api.dto;

import lombok.Data;
import sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CartDto {

    private String cartId;
    private String customerId;
    private String selectedCarrierId;
    private String selectedPaymentId;
    private List<CartItem> items;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String selectedPickupPointId;
    private String selectedPickupPointName;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private Instant createDate;
    private Instant updateDate;

    // Discount related fields
    private List<CartDiscountClientDto> appliedDiscounts;
    private BigDecimal totalPrice; // Products total
    private BigDecimal totalDiscount;
    private BigDecimal finalPrice; // Total - Discount + Carrier + Payment
    private boolean discountForNewsletter;

    private PriceBreakDown priceBreakDown;
}
