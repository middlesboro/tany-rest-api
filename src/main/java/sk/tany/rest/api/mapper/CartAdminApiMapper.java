package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateRequest;
import sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateResponse;
import sk.tany.rest.api.dto.admin.cart.get.CartAdminGetResponse;
import sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateResponse;

@Mapper(componentModel = "spring")
public interface CartAdminApiMapper {
    @Mapping(target = "cartId", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    CartDto toDto(CartAdminCreateRequest request);

    CartAdminCreateResponse toCreateResponse(CartDto dto);

    CartAdminGetResponse toGetResponse(CartDto dto);

    @Mapping(target = "cartId", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    CartDto toDto(CartAdminUpdateRequest request);

    CartAdminUpdateResponse toUpdateResponse(CartDto dto);
}
