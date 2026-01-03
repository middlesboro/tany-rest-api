package sk.tany.rest.api.service.client.payment;

import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;

public interface PaymentTypeService {
    PaymentType getSupportedType();
    PaymentInfoDto getPaymentInfo(OrderDto order, PaymentDto payment);
    default String checkStatus(String orderId) {
        return null;
    }
}
