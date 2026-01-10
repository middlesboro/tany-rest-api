package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.mapper.PaymentMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentAdminServiceImpl implements PaymentAdminService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public Page<PaymentDto> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(paymentMapper::toDto);
    }

    @Override
    public Optional<PaymentDto> findById(String id) {
        return paymentRepository.findById(id).map(paymentMapper::toDto);
    }

    @Override
    public PaymentDto save(PaymentDto paymentDto) {
        var payment = paymentMapper.toEntity(paymentDto);
        var savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public PaymentDto update(String id, PaymentDto paymentDto) {
        paymentDto.setId(id);
        var payment = paymentMapper.toEntity(paymentDto);
        var savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public PaymentDto patch(String id, sk.tany.rest.api.dto.admin.payment.patch.PaymentPatchRequest patchDto) {
        var payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        paymentMapper.updateEntityFromPatch(patchDto, payment);
        var savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public void deleteById(String id) {
        paymentRepository.deleteById(id);
    }
}
