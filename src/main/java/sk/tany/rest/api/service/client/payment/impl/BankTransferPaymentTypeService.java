package sk.tany.rest.api.service.client.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import scala.Option;
import scala.math.BigDecimal;
import sk.softwave.paybysquare.PayBySquare$;
import sk.softwave.paybysquare.SimplePay;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.service.client.payment.PaymentTypeService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class BankTransferPaymentTypeService implements PaymentTypeService {

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

    private String generateQrCode(OrderDto order) throws IOException {
        BigDecimal amount = new BigDecimal(order.getFinalPrice());
        String vsValue = getVariableSymbol(order);
        Option<String> vs = Option.apply(vsValue);
        Option<String> none = Option.apply(null);
        Option<String> bicOpt = Option.apply(bic);

        SimplePay pay = new SimplePay(
                amount,
                "EUR",
                vs,
                none,
                none,
                none,
                none,
                iban,
                bicOpt
        );

        File tempFile = Files.createTempFile("qr", ".png").toFile();
        try {
            PayBySquare$.MODULE$.encodePlainQR(pay, tempFile.getAbsolutePath(), 300, 4);
            byte[] fileContent = Files.readAllBytes(tempFile.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
