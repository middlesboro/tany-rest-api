package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.admin.order.create.OrderAdminCreateRequest;
import sk.tany.rest.api.dto.admin.order.create.OrderAdminCreateResponse;
import sk.tany.rest.api.dto.admin.order.get.OrderAdminGetResponse;
import sk.tany.rest.api.dto.admin.order.list.OrderAdminListResponse;
import sk.tany.rest.api.dto.admin.order.update.OrderAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.order.update.OrderAdminUpdateResponse;

@Mapper(componentModel = "spring")
public interface OrderAdminApiMapper {
    @Mapping(target = "id", ignore = true)
    OrderDto toDto(OrderAdminCreateRequest request);
    OrderAdminCreateResponse toCreateResponse(OrderDto dto);

    OrderAdminGetResponse toGetResponse(OrderDto dto);

    OrderAdminListResponse toListResponse(OrderDto dto);

    @Mapping(target = "id", ignore = true)
    OrderDto toDto(OrderAdminUpdateRequest request);
    OrderAdminUpdateResponse toUpdateResponse(OrderDto dto);
}
