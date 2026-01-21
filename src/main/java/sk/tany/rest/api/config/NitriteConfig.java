package sk.tany.rest.api.config;

import org.dizitart.no2.Nitrite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class NitriteConfig {

    private static final String DB_FILE = "tany.db";

    @Bean(destroyMethod = "close")
    public Nitrite nitrite() {
        return Nitrite.builder()
                .compressed() // V3 supports compressed()
                .filePath(new File(DB_FILE))
                .openOrCreate();
    }
}
