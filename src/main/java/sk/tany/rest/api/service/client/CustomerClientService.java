package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.CustomerDto;

import java.util.Optional;

public interface CustomerClientService {
    CustomerContextDto getCustomerContext(String cartId);
    CustomerDto findByEmail(String email);
    CustomerDto save(CustomerDto customerDto);
    Optional<CustomerDto> findById(String id);
    CustomerDto updateCustomer(CustomerDto customerDto);
}
