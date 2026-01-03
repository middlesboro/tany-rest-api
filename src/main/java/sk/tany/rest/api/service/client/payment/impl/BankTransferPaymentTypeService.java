package sk.tany.rest.api.service.client.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.service.client.payment.PaymentTypeService;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankTransferPaymentTypeService implements PaymentTypeService {

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.BANK_TRANSFER;
    }

    @Override
    public PaymentInfoDto getPaymentInfo(OrderDto order, PaymentDto payment) {
        return PaymentInfoDto.builder().build();
    }
}
