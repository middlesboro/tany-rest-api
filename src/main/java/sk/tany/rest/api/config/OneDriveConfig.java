package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "onedrive")
public class OneDriveConfig {
    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private boolean sendDocuments;
}
