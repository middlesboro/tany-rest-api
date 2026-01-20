package sk.tany.rest.api.domain.customer;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import sk.tany.rest.api.domain.order.Order;

import java.time.Instant;
import java.util.List;

@Data
public class Customer {

    @Id
    private String id;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String phone;
    private Role role;
    private Address address;
    private Address invoiceAddress;
    private Address deliveryAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private List<Order> orders;
    private Instant createdDate;
    private Instant updateDate;
}
