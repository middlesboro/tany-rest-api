package sk.tany.rest.api.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.mapper.PaymentMapper;
import sk.tany.rest.api.service.client.payment.PaymentTypeServiceFactory;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentClientServiceImpl implements PaymentClientService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentTypeServiceFactory paymentTypeServiceFactory;

    @Override
    public Page<PaymentDto> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(paymentMapper::toDto);
    }

    @Override
    public Optional<PaymentDto> findById(String id) {
        return paymentRepository.findById(id).map(paymentMapper::toDto);
    }

    @Override
    public PaymentInfoDto getPaymentInfo(OrderDto order) {
        PaymentDto payment = findById(order.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return paymentTypeServiceFactory.getService(payment.getType())
                .map(service -> service.getPaymentInfo(order, payment))
                .orElse(PaymentInfoDto.builder().build());
    }

}
