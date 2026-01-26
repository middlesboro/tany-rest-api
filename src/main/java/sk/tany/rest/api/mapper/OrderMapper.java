package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.OrderStatusHistoryDto;
import sk.tany.rest.api.dto.admin.order.patch.OrderPatchRequest;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface OrderMapper {
    OrderDto toDto(Order order);
    Order toEntity(OrderDto orderDto);
    OrderItemDto toDto(OrderItem orderItem);
    OrderItem toEntity(OrderItemDto orderItemDto);

    OrderStatusHistoryDto toDto(OrderStatusHistory history);
    OrderStatusHistory toEntity(OrderStatusHistoryDto historyDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(OrderPatchRequest patch, @MappingTarget Order order);
}
