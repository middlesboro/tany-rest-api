package sk.tany.rest.api.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class GlobalPaymentsSigner {

    public String sign(String text, String privateKeyContent, String password) {
        try {
            String privateKeyPEM = privateKeyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);

            PrivateKey privateKey;

            // Check if key is encrypted (naive check or try-catch strategy)
            // PKCS8EncodedKeySpec handles unencrypted keys.
            // If the key is encrypted, we need to decrypt it.
            // Since we don't have BouncyCastle and standard Java support for encrypted PEMs (PKCS#1 or PKCS#8 encrypted) is limited/complex without it,
            // we will attempt to parse as standard PKCS#8 first.

            try {
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                privateKey = kf.generatePrivate(spec);
            } catch (Exception e) {
                 // If failed, it might be encrypted. However, handling encrypted private keys without BouncyCastle
                 // is significantly complex (parsing ASN.1 structures).
                 // Given the environment constraints, we will log a warning if password is provided but we failed to use it
                 // via standard mechanisms, and re-throw.
                 // Ideally, we would use:
                 // EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(keyBytes);
                 // PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
                 // SecretKeyFactory skf = SecretKeyFactory.getInstance(epki.getAlgName());
                 // key = skf.generateSecret(keySpec);
                 // privateKeySpec = epki.getKeySpec(key);

                 // Let's try to support EncryptedPrivateKeyInfo if possible
                 try {
                     EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(keyBytes);
                     PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
                     SecretKeyFactory skf = SecretKeyFactory.getInstance(epki.getAlgName());
                     // This might fail if the algorithm is not supported by default providers (e.g. some PBEWithMD5AndDES)
                     PKCS8EncodedKeySpec pkcs8KeySpec = epki.getKeySpec(skf.generateSecret(pbeKeySpec));
                     KeyFactory kf = KeyFactory.getInstance("RSA");
                     privateKey = kf.generatePrivate(pkcs8KeySpec);
                 } catch (Exception innerE) {
                     log.error("Failed to decrypt private key. Ensure it is in PKCS#8 format. If encrypted, the algorithm must be supported by the JVM.", innerE);
                     throw e;
                 }
            }

            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(privateKey);
            signature.update(text.getBytes("UTF-8"));
            byte[] signedBytes = signature.sign();

            return Base64.getEncoder().encodeToString(signedBytes);
        } catch (Exception e) {
            log.error("Error signing text: {}", text, e);
            throw new RuntimeException("Failed to sign text", e);
        }
    }

    public boolean verify(String text, String signatureBase64, String publicKeyContent) {
        try {
            String publicKeyPEM = publicKeyContent
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey publicKey = kf.generatePublic(spec);

            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            signature.update(text.getBytes("UTF-8"));
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            return signature.verify(signatureBytes);
        } catch (Exception e) {
            log.error("Error verifying signature: {}", signatureBase64, e);
            return false;
        }
    }
}
