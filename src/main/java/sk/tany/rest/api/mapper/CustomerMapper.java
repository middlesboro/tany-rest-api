package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.admin.customer.patch.CustomerPatchRequest;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerDto toDto(Customer customer);

    Customer toEntity(CustomerDto customerDto);

    sk.tany.rest.api.domain.customer.Address toEntity(sk.tany.rest.api.dto.AddressDto addressDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(CustomerPatchRequest patch, @MappingTarget Customer customer);
}
