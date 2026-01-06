package sk.tany.rest.api.service.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GlobalPaymentsSignerTest {

    private final GlobalPaymentsSigner signer = new GlobalPaymentsSigner();

    // Keys from gpwebpay_public_keys.php and a dummy private key for testing
    // Since we don't have a matching private key for the public key in the PHP file (only the public key is there),
    // we should generate a pair for testing purposes.

    @Test
    public void testSignAndVerify() throws Exception {
        // Generate a test key pair
        java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        java.security.KeyPair keyPair = keyGen.generateKeyPair();

        String privateKeyContent = "-----BEGIN PRIVATE KEY-----\n" +
                java.util.Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                "\n-----END PRIVATE KEY-----";

        String publicKeyContent = "-----BEGIN PUBLIC KEY-----\n" +
                java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";

        String text = "MERCHANTNUMBER|OPERATION|ORDERNUMBER|AMOUNT|CURRENCY|DEPOSITFLAG|MERORDERNUM|URL|DESCRIPTION|MD|ADDINFO";

        // Sign
        String signature = signer.sign(text, privateKeyContent, "password"); // password ignored for PKCS8 in our impl for now
        assertNotNull(signature);

        // Verify
        boolean result = signer.verify(text, signature, publicKeyContent);
        assertTrue(result);

        // Verify with wrong text
        boolean resultFail = signer.verify(text + "modified", signature, publicKeyContent);
        assertFalse(resultFail);
    }
}
