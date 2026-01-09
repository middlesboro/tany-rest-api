package sk.tany.rest.api.dto;

import lombok.Data;
import sk.tany.rest.api.domain.customer.Role;

@Data
public class CustomerDto {

    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private Role role;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
}
