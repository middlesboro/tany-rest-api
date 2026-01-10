package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class CustomerDto {

    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
    private sk.tany.rest.api.domain.customer.Role role;
}
