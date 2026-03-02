package sk.tany.rest.api.service.client.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.domain.payment.GlobalPaymentsPaymentRepository;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.GlobalPaymentDetailsDto;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.dto.client.payment.PaymentCallbackDto;
import sk.tany.rest.api.event.OrderStatusChangedEvent;
import sk.tany.rest.api.service.client.payment.PaymentTypeService;
import sk.tany.rest.api.service.common.GlobalPaymentsSigner;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.domain.order.Order;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class GlobalPaymentsPaymentTypeService implements PaymentTypeService {

    private final GlobalPaymentsPaymentRepository globalPaymentsPaymentRepository;
    private final OrderRepository orderRepository;
    private final GlobalPaymentsSigner signer;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;
    private final sk.tany.rest.api.config.GpWebPayConfig gpWebPayConfig;

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.GLOBAL_PAYMENTS;
    }

    @Override
    public PaymentInfoDto getPaymentInfo(OrderDto order, PaymentDto payment) {
        String operation = "CREATE_ORDER";
        String orderNumber = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String amount = String.valueOf(order.getFinalPrice().multiply(BigDecimal.valueOf(100)).intValue());
        String currency = "978"; // EUR
        String depositFlag = "1";
        String merOrderNum = String.valueOf(order.getOrderIdentifier());

        String textToSign = gpWebPayConfig.getMerchantNumber() + "|" + operation + "|" + orderNumber + "|" + amount + "|" + currency + "|" + depositFlag + "|" + merOrderNum + "|" + gpWebPayConfig.getReturnUrl() + "||" + order.getId();

        String digest = signer.sign(textToSign, gpWebPayConfig.getPrivateKey(), gpWebPayConfig.getPrivateKeyPassword());

        return PaymentInfoDto.builder()
                .globalPaymentDetails(GlobalPaymentDetailsDto.builder()
                        .merchantNumber(gpWebPayConfig.getMerchantNumber())
                        .operation("CREATE_ORDER")
                        .orderNumber(orderNumber)
                        .amount(amount)
                        .currency(currency)
                        .depositFlag(depositFlag)
                        .merOrderNum(merOrderNum)
                        .url(gpWebPayConfig.getReturnUrl())
                        .paymentUrl(gpWebPayConfig.getUrl())
                        .md(order.getId())
                        .digest(digest)
                        .build())
                .build();
    }

    public String paymentCallback(PaymentCallbackDto paymentCallback) {
        String operation = paymentCallback.getOperation();
        String orderNumber = paymentCallback.getOrderNumber();
        String merOrderNum = paymentCallback.getMerOrderNum();
        String md = paymentCallback.getMd();
        String prCode = paymentCallback.getPrCode();
        String srCode = paymentCallback.getSrCode();
        String resultText = paymentCallback.getResultText();
        String digest = paymentCallback.getDigest1();

        if (!"0".equals(prCode) || !"0".equals(srCode)) {
            return "ERROR";
        }

        StringBuilder textToVerify = new StringBuilder();
        textToVerify.append(operation).append("|").append(orderNumber);
        textToVerify.append("|").append(merOrderNum);
        textToVerify.append("|").append(md);
        textToVerify.append("|").append(prCode).append("|").append(srCode);
        if (resultText != null && !resultText.isEmpty()) {
            textToVerify.append("|").append(resultText);
        }
        textToVerify.append("|").append(gpWebPayConfig.getMerchantNumber());
        boolean isValid = signer.verify(textToVerify.toString(), digest, gpWebPayConfig.getPublicKey());

        if (isValid) {
            orderRepository.findById(md).ifPresent(order -> {
                if (order.getStatus() != OrderStatus.PAID) {
                    order.setStatus(OrderStatus.PAID);
                    if (order.getStatusHistory() == null) {
                        order.setStatusHistory(new ArrayList<>());
                    }
                    order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.PAID, Instant.now()));
                    Order savedOrder = orderRepository.save(order);
                    eventPublisher.publishEvent(new OrderStatusChangedEvent(savedOrder));
                }
            });
        }

        return "PAID";
    }
}
