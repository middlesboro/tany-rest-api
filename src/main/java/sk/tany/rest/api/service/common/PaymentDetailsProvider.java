package sk.tany.rest.api.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import scala.Option;
import scala.math.BigDecimal;
import sk.softwave.paybysquare.PayBySquare$;
import sk.softwave.paybysquare.SimplePay;
import sk.tany.rest.api.dto.OrderDto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentDetailsProvider {

    @Value("${eshop.bank-account.iban}")
    private String iban;

    @Value("${eshop.bank-account.bic}")
    private String bic;

    public String getIban() {
        return iban;
    }

    public String getSwift() {
        return bic;
    }

    public String getVariableSymbol(OrderDto order) {
        return order.getOrderIdentifier() != null ? String.valueOf(order.getOrderIdentifier()) : null;
    }

    public String generateQrCodeBase64(OrderDto order) {
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
