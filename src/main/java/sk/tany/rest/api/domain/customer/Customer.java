package sk.tany.rest.api.domain.customer;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class Customer implements BaseEntity {

    @Id
    private String id;
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
    private Instant updateDate;

    @Override
    public Object getSortValue(String field) {
        switch (field) {
            case "firstname": return firstname;
            case "lastname": return lastname;
            case "email": return email;
            default: return BaseEntity.super.getSortValue(field);
        }
    }

    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
