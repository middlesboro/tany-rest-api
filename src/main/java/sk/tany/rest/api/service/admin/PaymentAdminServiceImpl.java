package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.mapper.PaymentMapper;
import sk.tany.rest.api.service.common.ImageService;
import sk.tany.rest.api.util.PriceCalculator;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentAdminServiceImpl implements PaymentAdminService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ImageService imageService;
    private final PriceCalculator priceCalculator;

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
        processPrice(paymentDto);
        var payment = paymentMapper.toEntity(paymentDto);
        var savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public PaymentDto update(String id, PaymentDto paymentDto) {
        paymentDto.setId(id);
        processPrice(paymentDto);
        var payment = paymentMapper.toEntity(paymentDto);
        var savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public void deleteById(String id) {
        var payment = paymentRepository.findById(id);
        if (payment.isPresent()) {
            if (payment.get().getImage() != null) {
                imageService.delete(payment.get().getImage());
            }
            paymentRepository.deleteById(id);
        }
    }

    private void processPrice(PaymentDto paymentDto) {
        if (paymentDto.getPrice() != null && paymentDto.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            paymentDto.setPrice(PriceCalculator.roundPrice(paymentDto.getPrice()));
            paymentDto.setPriceWithoutVat(priceCalculator.calculatePriceWithoutVat(paymentDto.getPrice()));
            paymentDto.setVatValue(PriceCalculator.roundPrice(paymentDto.getPrice().subtract(paymentDto.getPriceWithoutVat())));
        } else {
            paymentDto.setPrice(BigDecimal.ZERO);
            paymentDto.setPriceWithoutVat(BigDecimal.ZERO);
            paymentDto.setVatValue(BigDecimal.ZERO);
        }
    }
}
