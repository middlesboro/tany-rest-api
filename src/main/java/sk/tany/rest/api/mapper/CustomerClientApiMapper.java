package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.CustomerContextDto;
import sk.tany.rest.api.dto.client.customer.get.CustomerClientGetResponse;

@Mapper(componentModel = "spring")
public interface CustomerClientApiMapper {
    CustomerClientGetResponse toGetResponse(CustomerContextDto dto);
}
