package sk.tany.rest.api.config;

import org.dizitart.no2.Nitrite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class NitriteConfig {

    private static final String DB_FILE = "tany_encrypted.db";

    @Value("${admin.database.password}")
    private String databasePassword;
    @Value("${admin.database.username}")
    private String databaseUsername;

    @Bean(destroyMethod = "close")
    public Nitrite nitrite() {
        return Nitrite.builder()
                .compressed()
                .filePath(new File(DB_FILE))
                .openOrCreate(databaseUsername, databasePassword);
    }
}
