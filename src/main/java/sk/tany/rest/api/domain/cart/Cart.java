package sk.tany.rest.api.domain.cart;

import lombok.Data;
import sk.tany.rest.api.domain.customer.Address;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class Cart {

    private String id;
    private String cartId;
    private List<CartItem> items;
    private String carrierId;
    private String paymentId;
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
    private Instant createDate;
    private Instant updateDate;
}
