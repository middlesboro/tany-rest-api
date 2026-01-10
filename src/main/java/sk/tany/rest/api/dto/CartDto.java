package sk.tany.rest.api.dto;

import lombok.Data;

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
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
    private Instant createDate;
    private Instant updateDate;
}
