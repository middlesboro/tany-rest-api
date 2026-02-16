package sk.tany.rest.api.dto.client.customer.update;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.tany.rest.api.dto.AddressDto;

@Data
public class CustomerClientUpdateRequest {
    private String firstname;
    private String lastname;
    private String phone;
    @Valid
    private AddressDto invoiceAddress;
    @Valid
    private AddressDto deliveryAddress;
}
