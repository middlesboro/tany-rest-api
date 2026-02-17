package sk.tany.rest.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
@ConditionalOnProperty(name = "spring.data.mongodb.auditing.enabled", havingValue = "true", matchIfMissing = true)
public class MongoConfig {
}
