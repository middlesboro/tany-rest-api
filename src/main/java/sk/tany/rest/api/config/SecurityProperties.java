package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private Integer accessTokenValidity;
    private List<String> excludedUrls = new ArrayList<>();
}
