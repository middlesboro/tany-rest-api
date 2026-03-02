package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mailersend")
public class MailerSendConfig {
    private String apiToken;
    private String fromEmail;
    private String fromName;
}
