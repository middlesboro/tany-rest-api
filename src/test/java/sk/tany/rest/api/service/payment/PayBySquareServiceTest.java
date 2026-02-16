package sk.tany.rest.api.service.payment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

public class PayBySquareServiceTest {

    private final PayBySquareService service = new PayBySquareService();

    @Test
    public void testEncode_Simple() throws IOException {
        BigDecimal amount = new BigDecimal("10.00");
        String currency = "EUR";
        String vs = "1234567890";
        String iban = "SK1234567890123456789012";
        String bic = "SUBASKBA";

        String encoded = service.generateQrString(amount, currency, vs, null, null, null, null, iban, bic, null);

        // Expected value from previous run (without name)
        Assertions.assertEquals("0004K00060B82HP09BV1137Q32VUFD5JUEP5QC93Q6LRL6B7FM1J56BFJGQ86RTQNP9SM1TLHGA9VCN18IOB9PNKDH3IUUO", encoded);
    }

    @Test
    public void testEncode_WithBeneficiaryName() throws IOException {
        BigDecimal amount = new BigDecimal("10.00");
        String currency = "EUR";
        String vs = "1234567890";
        String iban = "SK1234567890123456789012";
        String bic = "SUBASKBA";
        String name = "Tany.sk";

        String encoded = service.generateQrString(amount, currency, vs, null, null, null, null, iban, bic, name);

        // The value will change due to appended name. We assert it is NOT null and NOT equal to the simple one.
        Assertions.assertNotNull(encoded);
        Assertions.assertNotEquals("0004K00060B82HP09BV1137Q32VUFD5JUEP5QC93Q6LRL6B7FM1J56BFJGQ86RTQNP9SM1TLHGA9VCN18IOB9PNKDH3IUUO", encoded);
        System.out.println("Encoded with name: " + encoded);
    }
}
