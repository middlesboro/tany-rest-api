package sk.tany.rest.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "email.imap")
public class EmailImapConfig {
    private String host;
    private int port;
    private String username;
    private String password;
    private String folder;
}
