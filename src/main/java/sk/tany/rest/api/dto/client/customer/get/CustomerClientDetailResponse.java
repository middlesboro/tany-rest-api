package sk.tany.rest.api.dto.client.customer.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.dto.AddressDto;

@Data
public class CustomerClientDetailResponse {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;
}
