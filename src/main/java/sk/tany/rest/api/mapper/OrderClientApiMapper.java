package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateRequest;
import sk.tany.rest.api.dto.client.order.create.OrderClientCreateResponse;
import sk.tany.rest.api.dto.client.order.get.OrderClientGetResponse;
import sk.tany.rest.api.dto.client.order.list.OrderClientListResponse;

@Mapper(componentModel = "spring")
public interface OrderClientApiMapper {
    @Mapping(target = "id", ignore = true)
    OrderDto toDto(OrderClientCreateRequest request);
    OrderClientCreateResponse toCreateResponse(OrderDto dto);

    OrderClientListResponse toListResponse(OrderDto dto);

    OrderClientGetResponse toGetResponse(OrderDto dto);
}
