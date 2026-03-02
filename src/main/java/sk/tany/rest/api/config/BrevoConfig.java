package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "brevo")
public class BrevoConfig {
    private String apiKey;
    private String fromEmail;
    private String fromName;
}
