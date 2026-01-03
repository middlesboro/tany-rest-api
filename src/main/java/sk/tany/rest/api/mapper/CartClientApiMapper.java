package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.client.cart.carrier.CartClientSetCarrierResponse;
import sk.tany.rest.api.dto.client.cart.payment.CartClientSetPaymentResponse;

@Mapper(componentModel = "spring")
public interface CartClientApiMapper {
    CartClientSetCarrierResponse toSetCarrierResponse(CartDto dto);
    CartClientSetPaymentResponse toSetPaymentResponse(CartDto dto);
}
