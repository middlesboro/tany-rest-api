package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "besteron")
public class BesteronConfig {
    private String clientId;
    private String clientSecret;
    private String apiKey;
    private String baseUrl;
    private String verifyUrl;
    private String returnUrl;
    private String notificationUrl;
}
