package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;

import java.util.Optional;

public interface PaymentClientService {
    Page<PaymentDto> findAll(Pageable pageable);
    Optional<PaymentDto> findById(String id);
    PaymentInfoDto getPaymentInfo(String orderId);
    String getPaymentStatus(String orderId);
}
