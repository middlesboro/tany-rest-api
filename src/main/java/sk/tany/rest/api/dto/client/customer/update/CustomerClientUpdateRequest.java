package sk.tany.rest.api.dto.client.customer.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CustomerClientUpdateRequest {
    private String firstname;
    private String lastname;
    private String email;
    private AddressDto invoiceAddress;
    private AddressDto deliveryAddress;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private String street;
        private String city;
        private String zip;
    }
}
