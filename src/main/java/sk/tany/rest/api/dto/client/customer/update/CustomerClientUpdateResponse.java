package sk.tany.rest.api.dto.client.customer.update;

import lombok.Data;
import sk.tany.rest.api.dto.AddressDto;

@Data
public class CustomerClientUpdateResponse {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
}
