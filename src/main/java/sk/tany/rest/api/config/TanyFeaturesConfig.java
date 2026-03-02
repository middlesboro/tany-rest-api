package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tany.features")
public class TanyFeaturesConfig {
    private String url = "http://localhost:8081";
    private String apiKey;
}
