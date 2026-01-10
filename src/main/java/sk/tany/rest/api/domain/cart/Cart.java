package sk.tany.rest.api.domain.cart;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import sk.tany.rest.api.domain.customer.Address;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "carts")
public class Cart {

    @Id
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
    private Address invoiceAddress;
    private Address deliveryAddress;
    @CreatedDate
    private Instant createDate;
    @LastModifiedDate
    private Instant updateDate;
}
