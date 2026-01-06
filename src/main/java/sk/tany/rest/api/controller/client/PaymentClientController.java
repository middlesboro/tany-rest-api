package sk.tany.rest.api.controller.client;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.dto.client.payment.PaymentCallbackDto;
import sk.tany.rest.api.dto.client.payment.PaymentCallbackRequest;
import sk.tany.rest.api.dto.client.payment.PaymentCallbackResponse;
import sk.tany.rest.api.dto.client.payment.PaymentStatusResponse;
import sk.tany.rest.api.service.client.PaymentClientService;
import sk.tany.rest.api.service.client.payment.impl.BesteronPaymentTypeService;
import sk.tany.rest.api.service.client.payment.impl.GlobalPaymentsPaymentTypeService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentClientController {

    private final PaymentClientService paymentService;
    private final GlobalPaymentsPaymentTypeService globalPaymentsPaymentService;
    private final BesteronPaymentTypeService besteronPaymentTypeService;

    @GetMapping
    public Page<PaymentDto> getPayments(Pageable pageable) {
        return paymentService.findAll(pageable);
    }

    @GetMapping("/info/{orderId}")
    public PaymentInfoDto getPaymentInfo(@PathVariable String orderId) {
        return paymentService.getPaymentInfo(orderId);
    }

    // todo optimalizacie na odstranenie urcitych znakov ako v php
    // todo remove to besteron status
    @GetMapping("/besteron-status/{orderId}")
    public PaymentStatusResponse getBesteronPaymentStatus(@PathVariable String orderId) {
        return new PaymentStatusResponse(besteronPaymentTypeService.checkStatus(orderId));
    }

    @PostMapping("/global-payments-callback")
    public PaymentCallbackResponse globalPaymentsCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        return new PaymentCallbackResponse(
                globalPaymentsPaymentService
                    .paymentCallback(PaymentCallbackDto.builder()
                            .operation(request.getOperation())
                            .orderNumber(request.getOrderNumber())
                            .merOrderNum(request.getMerOrderNum())
                            .md(request.getMd())
                            .orderId(request.getMd())
                            .prCode(request.getPrcode())
                            .srCode(request.getSrcode())
                            .resultText(request.getResultText())
                            .digest(request.getDigest())
                            .digest1(request.getDigest1())
                            .build()));
    }

    @GetMapping("/besteron-callback")
    public PaymentCallbackResponse besteronCallback(@RequestParam("transactionId") String transactionId) {
        besteronPaymentTypeService.paymentCallback(transactionId);
        return new PaymentCallbackResponse("OK");
    }

}
