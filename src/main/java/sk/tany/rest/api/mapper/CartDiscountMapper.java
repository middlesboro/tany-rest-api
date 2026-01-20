package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sk.tany.rest.api.domain.cartdiscount.CartDiscount;
import sk.tany.rest.api.dto.admin.cartdiscount.CartDiscountDto;
import sk.tany.rest.api.dto.admin.cartdiscount.create.CartDiscountCreateRequest;
import sk.tany.rest.api.dto.admin.cartdiscount.list.CartDiscountListResponse;
import sk.tany.rest.api.dto.admin.cartdiscount.update.CartDiscountUpdateRequest;
import sk.tany.rest.api.dto.client.cartdiscount.CartDiscountClientDto;

@Mapper(componentModel = "spring")
public interface CartDiscountMapper {

    CartDiscountDto toDto(CartDiscount cartDiscount);

    CartDiscount toEntity(CartDiscountCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    void updateEntityFromDto(CartDiscountUpdateRequest request, @MappingTarget CartDiscount cartDiscount);

    CartDiscountListResponse toListResponse(CartDiscount cartDiscount);

    CartDiscountClientDto toClientDto(CartDiscount cartDiscount);
}
