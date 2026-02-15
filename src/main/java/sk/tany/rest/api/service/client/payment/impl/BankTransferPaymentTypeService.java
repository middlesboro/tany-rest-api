package sk.tany.rest.api.service.client.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.service.client.payment.PaymentTypeService;
import sk.tany.rest.api.service.payment.PayBySquareService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankTransferPaymentTypeService implements PaymentTypeService {

    private final PayBySquareService payBySquareService;

    @Value("${eshop.bank-account.iban}")
    private String iban;

    @Value("${eshop.bank-account.bic}")
    private String bic;

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.BANK_TRANSFER;
    }

    @Override
    public PaymentInfoDto getPaymentInfo(OrderDto order, PaymentDto payment) {
        return PaymentInfoDto.builder()
                .iban(getIban())
                .swift(getSwift())
                .variableSymbol(getVariableSymbol(order))
                .qrCode(generateQrCodeBase64(order))
                .paymentLink(generatePaymeLink(order))
                .build();
    }

    private String getIban() {
        return iban;
    }

    private String getSwift() {
        return bic;
    }

    private String getVariableSymbol(OrderDto order) {
        return order.getOrderIdentifier() != null ? String.valueOf(order.getOrderIdentifier()) : null;
    }

    private String generateQrCodeBase64(OrderDto order) {
        try {
            return generateQrCode(order);
        } catch (Exception e) {
            log.error("Error generating QR code for order {}", order.getId(), e);
            return null;
        }
    }

    private String generatePaymeLink(OrderDto order) {
        try {
            String vs = getVariableSymbol(order);
            if (vs == null) {
                return null;
            }
            String msg = "Objednavka VS: " + vs;

            return "https://payme.sk?V=1" +
                    "&IBAN=" + iban.replace(" ", "") +
                    "&AM=" + String.format(Locale.US, "%.2f", order.getFinalPrice()) + // Locale.US to ensure dot as decimal separator
                    "&CC=EUR" +
                    "&MSG=" + URLEncoder.encode(msg, StandardCharsets.UTF_8) +
                    "&CN=" + URLEncoder.encode("Bc. Tatiana Grňová - Tany.sk", StandardCharsets.UTF_8); // todo change to config
        } catch (Exception e) {
            log.error("Error generating Payme link for order {}", order.getId(), e);
            return null;
        }
    }

    private String generateQrCode(OrderDto order) {
        return payBySquareService.generateQrCode(
                order.getFinalPrice(),
                "EUR",
                getVariableSymbol(order),
                null,
                null,
                null,
                null,
                iban,
                bic
        );
    }
}
