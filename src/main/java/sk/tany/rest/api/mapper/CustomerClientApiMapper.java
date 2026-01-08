package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.CustomerDto;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientGetResponse;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateRequest;
import sk.tany.rest.api.dto.client.customer.update.CustomerClientUpdateResponse;

@Mapper(componentModel = "spring")
public interface CustomerClientApiMapper {
    CustomerClientGetResponse toGetResponse(CustomerContextDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    CustomerDto toDto(CustomerClientUpdateRequest request);

    CustomerClientUpdateResponse toUpdateResponse(CustomerDto dto);
}
