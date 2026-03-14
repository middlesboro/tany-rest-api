package sk.tany.rest.api.dto.admin.customer.get;

import lombok.Data;
import sk.tany.rest.api.domain.customer.Role;
import sk.tany.rest.api.dto.AddressDto;

@Data
public class CustomerAdminGetResponse {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private Role role;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
    private String preferredPacketaBranchId;
    private String preferredPacketaBranchName;
    private String preferredBalikovoBranchId;
    private String preferredBalikovoBranchName;
}
