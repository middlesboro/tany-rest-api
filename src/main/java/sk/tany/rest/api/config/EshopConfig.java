package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "eshop")
public class EshopConfig {
    private String frontendUrl;
    private String frontendAdminUrl;
}
