package sk.tany.rest.api.service.client.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import sk.tany.rest.api.service.client.payment.PaymentTypeService;
import sk.tany.rest.api.service.common.GlobalPaymentsSigner;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.domain.order.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
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

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    private String emailPaidTemplate;

    // todo load as config class
    @Value("${gpwebpay.merchant-number}")
    private String merchantNumber;

    @Value("${gpwebpay.private-key}")
    private String privateKey;

    @Value("${gpwebpay.public-key}")
    private String publicKey;

    @Value("${gpwebpay.private-key-password}")
    private String privateKeyPassword;

    @Value("${gpwebpay.return-url}")
    private String returnUrl;

    @Value("${gpwebpay.url}")
    private String url;

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

        String textToSign = merchantNumber + "|" + operation + "|" + orderNumber + "|" + amount + "|" + currency + "|" + depositFlag + "|" + merOrderNum + "|" + returnUrl + "||" + order.getId();

        String digest = signer.sign(textToSign, privateKey, privateKeyPassword);

        return PaymentInfoDto.builder()
                .globalPaymentDetails(GlobalPaymentDetailsDto.builder()
                        .merchantNumber(merchantNumber)
                        .operation("CREATE_ORDER")
                        .orderNumber(orderNumber)
                        .amount(amount)
                        .currency(currency)
                        .depositFlag(depositFlag)
                        .merOrderNum(merOrderNum)
                        .url(returnUrl)
                        .paymentUrl(url)
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
        textToVerify.append("|").append(merchantNumber);
        boolean isValid = signer.verify(textToVerify.toString(), digest, publicKey);

        if (isValid) {
            orderRepository.findById(md).ifPresent(order -> {
                if (order.getStatus() != OrderStatus.PAID) {
                    order.setStatus(OrderStatus.PAID);
                    if (order.getStatusHistory() == null) {
                        order.setStatusHistory(new ArrayList<>());
                    }
                    order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.PAID, Instant.now()));
                    Order savedOrder = orderRepository.save(order);
                    sendOrderPaidEmail(savedOrder);
                }
            });
        }

        return "PAID";
    }

    private void sendOrderPaidEmail(Order order) {
        if (order.getEmail() == null || order.getEmail().isEmpty()) {
            log.warn("Cannot send 'Order Paid' email: Customer email is missing for order {}", order.getOrderIdentifier());
            return;
        }
        try {
            String template = getEmailPaidTemplate();

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Customer";
            String orderIdentifier = order.getOrderIdentifier() != null ? order.getOrderIdentifier().toString() : "";
            String orderConfirmationLink = frontendUrl + "/order/confirmation/" + order.getId();

            String body = template
                    .replace("{{firstname}}", firstname)
                    .replace("{{orderIdentifier}}", orderIdentifier)
                    .replace("{{orderConfirmationLink}}", orderConfirmationLink)
                    .replace("{{currentYear}}", String.valueOf(java.time.Year.now().getValue()));

            emailService.sendEmail(order.getEmail(), "Objednávka zaplatená", body, true, null);
            log.info("Sent 'Order Paid' email for order {}", order.getOrderIdentifier());

        } catch (Exception e) {
            log.error("Failed to send 'Order Paid' email for order {}", order.getOrderIdentifier(), e);
        }
    }

    private String getEmailPaidTemplate() throws java.io.IOException {
        if (emailPaidTemplate == null) {
            ClassPathResource resource = new ClassPathResource("templates/email/order_paid.html");
            byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
            emailPaidTemplate = new String(data, StandardCharsets.UTF_8);
        }
        return emailPaidTemplate;
    }

}
