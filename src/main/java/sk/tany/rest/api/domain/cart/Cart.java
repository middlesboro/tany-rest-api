package sk.tany.rest.api.domain.cart;

import lombok.Data;
import sk.tany.rest.api.domain.BaseEntity;
import sk.tany.rest.api.domain.customer.Address;
import sk.tany.rest.api.dto.PriceBreakDown;

import java.util.List;

@Data
public class Cart extends BaseEntity {
    private String cartId;
    private String customerId;
    private List<CartItem> items;
    private String selectedCarrierId;
    private String selectedPaymentId;
    private String selectedPickupPointId;
    private String selectedPickupPointName;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private Address invoiceAddress;
    private Address deliveryAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private List<String> discountCodes;
    private boolean discountForNewsletter;
    private PriceBreakDown priceBreakDown;
}
