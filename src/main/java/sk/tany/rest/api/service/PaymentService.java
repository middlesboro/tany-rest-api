package sk.tany.rest.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.PaymentDto;

import java.util.Optional;

public interface PaymentService {
    Page<PaymentDto> findAll(Pageable pageable);
    Optional<PaymentDto> findById(String id);
    PaymentDto save(PaymentDto paymentDto);
    PaymentDto update(String id, PaymentDto paymentDto);
    void deleteById(String id);
}
