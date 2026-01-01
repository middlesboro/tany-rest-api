package sk.tany.rest.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.dto.PaymentDto;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto toDto(Payment payment);
    Payment toEntity(PaymentDto paymentDto);
    void updateEntityFromDto(PaymentDto paymentDto, @MappingTarget Payment payment);
}
