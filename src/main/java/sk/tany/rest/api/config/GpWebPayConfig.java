package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gpwebpay")
public class GpWebPayConfig {
    private String merchantNumber;
    private String privateKey;
    private String publicKey;
    private String privateKeyPassword;
    private String returnUrl;
    private String url;
}
