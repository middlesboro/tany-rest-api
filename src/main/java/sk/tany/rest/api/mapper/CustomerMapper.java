package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.dto.CustomerDto;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDto toDto(Customer customer);

    Customer toEntity(CustomerDto customerDto);

    sk.tany.rest.api.domain.customer.Address toEntity(sk.tany.rest.api.dto.AddressDto addressDto);
}
