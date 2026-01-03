package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.service.client.PaymentClientService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentClientController {

    private final PaymentClientService paymentService;

    @GetMapping
    public Page<PaymentDto> getPayments(Pageable pageable) {
        return paymentService.findAll(pageable);
    }

    @GetMapping("/info/{orderId}")
    public PaymentInfoDto getPaymentInfo(@PathVariable String orderId) {
        return paymentService.getPaymentInfo(orderId);
    }

    @GetMapping("/status/{orderId}")
    public String getPaymentStatus(@PathVariable String orderId) {
        return paymentService.getPaymentStatus(orderId);
    }
}
