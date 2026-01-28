package sk.tany.rest.api.domain.cart;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import sk.tany.rest.api.domain.customer.Address;

import java.time.Instant;
import java.util.List;

@Data
public class Cart {

    @Id
    private String id;
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
    private Instant createDate;
    private Instant updateDate;
}
