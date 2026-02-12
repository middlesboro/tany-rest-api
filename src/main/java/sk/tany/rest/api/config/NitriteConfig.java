package sk.tany.rest.api.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.common.mapper.JacksonMapperModule;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class NitriteConfig {

    public static final String DB_FILE = "tany_encrypted.db";

    @Value("${admin.database.password}")
    private String databasePassword;
    @Value("${admin.database.username}")
    private String databaseUsername;

    @Bean(destroyMethod = "close")
    public Nitrite nitrite() {
        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath(new File(DB_FILE))
                .compress(true)
                .build();

        return Nitrite.builder()
                .loadModule(storeModule)
                .loadModule(new JacksonMapperModule(new JavaTimeModule()))
                .openOrCreate(databaseUsername, databasePassword);
    }
}
