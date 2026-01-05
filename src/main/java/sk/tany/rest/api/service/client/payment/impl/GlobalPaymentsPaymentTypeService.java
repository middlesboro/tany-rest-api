package sk.tany.rest.api.service.client.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.payment.GlobalPaymentsPayment;
import sk.tany.rest.api.domain.payment.GlobalPaymentsPaymentRepository;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.service.client.payment.PaymentTypeService;
import sk.tany.rest.api.service.common.GlobalPaymentsSigner;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GlobalPaymentsPaymentTypeService implements PaymentTypeService {

    private final GlobalPaymentsPaymentRepository globalPaymentsPaymentRepository;
    private final CustomerRepository customerRepository;

    @Value("${eshop.base-url}")
    private String baseUrl;

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.GLOBAL_PAYMENTS;
    }

    @Override
    public PaymentInfoDto getPaymentInfo(OrderDto order, PaymentDto payment) {
        String paymentLink = baseUrl + "/api/payments/global-payments/redirect/" + order.getId();
        return PaymentInfoDto.builder()
                .paymentLink(paymentLink)
                .build();
    }

    @Override
    public String checkStatus(String orderId) {
        Optional<GlobalPaymentsPayment> paymentOpt = globalPaymentsPaymentRepository.findTopByOrderIdOrderByCreateDateDesc(orderId);
        return paymentOpt.map(GlobalPaymentsPayment::getStatus).orElse(null);
    }
}
