package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.client.cart.carrier.CartClientSetCarrierResponse;
import sk.tany.rest.api.dto.client.cart.payment.CartClientSetPaymentResponse;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateRequest;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateResponse;

@Mapper(componentModel = "spring")
public interface CartClientApiMapper {

    @Mapping(target = "priceBreakDown", source = "priceBreakDown")
    CartClientSetCarrierResponse toSetCarrierResponse(CartDto cartDto);

    CartClientSetPaymentResponse toSetPaymentResponse(CartDto cartDto);

    @Mapping(target = "items", ignore = true) // Complex mapping, usually ignored or handled specifically if structures differ significantly. But here they match field-wise, MapStruct might need help if types are different.
    CartDto toDto(CartClientUpdateRequest request);

    @Mapping(target = "priceBreakDown", source = "priceBreakDown")
    CartClientUpdateResponse toUpdateResponse(CartDto cartDto);
}
