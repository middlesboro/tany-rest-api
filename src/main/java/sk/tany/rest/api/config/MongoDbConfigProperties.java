package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.mongodb")
public class MongoDbConfigProperties {
    private String database = "tany";
    private String uri;
    private String masterKey;
    private String cryptLibPath;
}
