package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.dto.CartDto;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartDto toDto(Cart cart);

    Cart toEntity(CartDto cartDto);
}
