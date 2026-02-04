package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "isklad")
public class ISkladProperties {
    private boolean enabled;
    private String url;
    private String authId;
    private String authKey;
    private String authToken;
    private Integer shopSettingId;
    private String incomingApiKey;
}
