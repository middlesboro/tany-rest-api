package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.admin.cart.patch.CartPatchRequest;

import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CartMapper {

    CartDto toDto(Cart cart);

    Cart toEntity(CartDto cartDto);

    void updateEntityFromDto(CartDto dto, @MappingTarget Cart entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(CartPatchRequest patch, @MappingTarget Cart entity);
}
