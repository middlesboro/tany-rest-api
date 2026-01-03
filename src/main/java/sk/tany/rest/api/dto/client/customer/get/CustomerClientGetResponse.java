package sk.tany.rest.api.dto.client.customer.get;

import lombok.Data;
import sk.tany.rest.api.dto.CustomerContextCartDto;
import sk.tany.rest.api.dto.CustomerDto;

@Data
public class CustomerClientGetResponse {
    private CustomerDto customerDto;
    private CustomerContextCartDto cartDto;
}
