package sk.tany.rest.api.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.admin.payment.patch.PaymentPatchRequest;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(Payment payment);
    Payment toEntity(PaymentDto paymentDto);
    void updateEntityFromDto(PaymentDto paymentDto, @MappingTarget Payment payment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(PaymentPatchRequest patch, @MappingTarget Payment payment);
}
