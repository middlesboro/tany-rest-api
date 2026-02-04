package sk.tany.rest.api.config;

import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Slf4j
@Configuration
public class NitriteConfig {

    private static final String DB_FILE = "tany_encrypted.db";

    @Value("${admin.database.password}")
    private String databasePassword;
    @Value("${admin.database.username}")
    private String databaseUsername;
    @Value("${admin.database.path}")
    private String databasePath;

    @Bean(destroyMethod = "close")
    public Nitrite nitrite() {
        File dbFile = new File(databasePath);
        File parentDir = dbFile.getParentFile();

        log.info("--- DIAGNOSTIKA DATABÁZY ---");
        log.info("Cieľová cesta k DB: {}", dbFile.getAbsolutePath());

        if (parentDir != null && parentDir.exists()) {
            log.info("Priečinok '{}' existuje.", parentDir.getAbsolutePath());
            String[] files = parentDir.list();
            if (files != null && files.length > 0) {
                log.info("Súbory v priečinku: {}", String.join(", ", files));
            } else {
                log.info("Priečinok je prázdny.");
            }
        } else {
            log.error("POZOR: Priečinok '{}' NEEXISTUJE! Nitrite sa ho pokúsi vytvoriť, ak má práva.",
                    parentDir != null ? parentDir.getAbsolutePath() : "koreňový priečinok");
        }

        log.info("Otváram Nitrite DB pre používateľa: {}", databaseUsername);
        log.info("Otváram Nitrite DB pre password: {}", databasePassword);
        log.info("Otváram Nitrite DB: {}", databasePath);

        try {
            Class.forName("org.h2.store.fs.FilePathDisk");
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

//        return Nitrite.builder()
//                .compressed()
//                .filePath(databasePath)
//                .openOrCreate(databaseUsername, databasePassword);

        return Nitrite.builder()
                .compressed()
                .filePath("tany.db")
                .openOrCreate();
    }
}
