package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "imagekit")
public class ImageKitProperties {
    private String urlEndpoint;
    private String publicKey;
    private String privateKey;
}
