package sk.tany.rest.api.domain.customer;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Customer extends BaseEntity {
private String email;
    private String firstname;
    private String lastname;
    private String phone;
    private Role role = Role.CUSTOMER;
    private Address address;
    private Address invoiceAddress;
    private Address deliveryAddress;
    private boolean deliveryAddressSameAsInvoiceAddress;
    private Instant createdDate;
@Override
    public Object getSortValue(String field) {
        switch (field) {
            case "firstname": return firstname;
            case "lastname": return lastname;
            case "email": return email;
            default: return super.getSortValue(field);
        }
    }
}
