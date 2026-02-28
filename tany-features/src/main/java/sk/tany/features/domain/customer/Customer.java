package sk.tany.features.domain.customer;

import lombok.Data;
import sk.tany.features.domain.BaseEntity;

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
    private String preferredPacketaBranchId;
    private String preferredPacketaBranchName;
    private String preferredBalikovoBranchId;
    private String preferredBalikovoBranchName;
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
