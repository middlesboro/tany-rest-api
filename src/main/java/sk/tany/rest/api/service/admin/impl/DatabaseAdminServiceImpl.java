package sk.tany.rest.api.service.admin.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.config.NitriteConfig;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;
import sk.tany.rest.api.service.admin.DatabaseAdminService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseAdminServiceImpl implements DatabaseAdminService {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final Nitrite nitrite;

    @Override
    public void importDatabase(MultipartFile file) {
        if (!nitrite.isClosed()) {
            nitrite.close();
        }
        File dbFile = new File(NitriteConfig.DB_FILE);
        try {
            Files.copy(file.getInputStream(), dbFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("Database imported successfully. Application restart required.");
        } catch (IOException e) {
            log.error("Failed to import database", e);
            throw new RuntimeException("Failed to import database", e);
        }
    }

    @Override
    public File exportDatabaseToJson() {
        try {
            Path tempDir = Files.createTempDirectory("tany_export_");
            Map<String, AbstractInMemoryRepository> repositories = applicationContext.getBeansOfType(AbstractInMemoryRepository.class);

            for (AbstractInMemoryRepository repository : repositories.values()) {
                List<?> all = repository.findAll();
                if (!all.isEmpty()) {
                    String className = repository.getEntityType().getSimpleName();
                    File file = tempDir.resolve(className + ".json").toFile();
                    objectMapper.writeValue(file, all);
                }
            }

            Path zipFile = Files.createTempFile("tany_export_", ".zip");
            zipDirectory(tempDir, zipFile);

            // Cleanup temp dir
            Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a)) // Delete files first
                .map(Path::toFile)
                .forEach(File::delete);

            return zipFile.toFile();

        } catch (IOException e) {
            throw new RuntimeException("Failed to export database", e);
        }
    }

    @Override
    public void importDatabaseFromJson(File input) {
        if (input.isDirectory()) {
            importFromDirectory(input);
        } else {
             throw new IllegalArgumentException("Input must be a directory containing JSON files");
        }
    }

    private void importFromDirectory(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        Map<String, AbstractInMemoryRepository> repositories = applicationContext.getBeansOfType(AbstractInMemoryRepository.class);

        for (File jsonFile : files) {
            String entityName = jsonFile.getName().replace(".json", "");
            AbstractInMemoryRepository repository = findRepositoryForEntity(repositories, entityName);

            if (repository != null) {
                try {
                    Class<?> type = repository.getEntityType();
                    log.info("Importing {} from {}", type.getSimpleName(), jsonFile.getName());
                    List<?> entities = objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, type));

                    if (!entities.isEmpty()) {
                        repository.deleteAll();
                        // Unchecked cast is safe here because we deserialized to type T
                        repository.saveAll((Iterable) entities);
                        log.info("Imported {} entities.", entities.size());
                    }
                } catch (IOException e) {
                    log.error("Failed to import file: {}", jsonFile.getName(), e);
                }
            } else {
                log.warn("No repository found for entity type: {}", entityName);
            }
        }
    }

    private AbstractInMemoryRepository findRepositoryForEntity(Map<String, AbstractInMemoryRepository> repositories, String entityName) {
        return repositories.values().stream()
                .filter(repo -> repo.getEntityType().getSimpleName().equals(entityName))
                .findFirst()
                .orElse(null);
    }

    private void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walk(sourceDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());
                    try {
                        zos.putNextEntry(zipEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        log.error("Error zipping file: " + path, e);
                    }
                });
        }
    }
}
