package sk.tany.rest.api.dto.admin.customer.list;

import lombok.Data;
import sk.tany.rest.api.domain.customer.Role;
import sk.tany.rest.api.dto.AddressDto;

@Data
public class CustomerAdminListResponse {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
}
