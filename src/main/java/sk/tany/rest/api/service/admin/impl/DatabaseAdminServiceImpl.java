package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.ProductSearchEngine;
import sk.tany.rest.api.service.admin.DatabaseAdminService;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseAdminServiceImpl implements DatabaseAdminService {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final ProductSearchEngine productSearchEngine;

    @Override
    public File exportDatabaseToJson() {
        try {
            Path tempDir = Files.createTempDirectory("tany_export_");
            Repositories repositories = new Repositories(applicationContext);

            for (Class<?> domainType : repositories) {
                Object repo = repositories.getRepositoryFor(domainType).orElse(null);
                if (repo instanceof MongoRepository) {
                    MongoRepository mongoRepo = (MongoRepository) repo;
                    List<?> all = mongoRepo.findAll();
                    if (!all.isEmpty()) {
                        String className = domainType.getSimpleName();
                        File file = tempDir.resolve(className + ".json").toFile();
                        objectMapper.writeValue(file, all);
                    }
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

        Repositories repositories = new Repositories(applicationContext);

        for (File jsonFile : files) {
            String entityName = jsonFile.getName().replace(".json", "");

            Object repository = null;
            Class<?> entityType = null;

            for (Class<?> domainType : repositories) {
                if (domainType.getSimpleName().equals(entityName)) {
                    repository = repositories.getRepositoryFor(domainType).orElse(null);
                    entityType = domainType;
                    break;
                }
            }

            if (repository instanceof MongoRepository && entityType != null) {
                MongoRepository mongoRepo = (MongoRepository) repository;
                log.info("Importing {} from {}", entityType.getSimpleName(), jsonFile.getName());
                List<?> entities = objectMapper.readValue(jsonFile, objectMapper.getTypeFactory().constructCollectionType(List.class, entityType));

                if (!entities.isEmpty()) {
                    mongoRepo.deleteAll();
                    mongoRepo.saveAll(entities);
                    log.info("Imported {} entities.", entities.size());
                }
            } else {
                log.warn("No repository found for entity type: {}", entityName);
            }
        }
        productSearchEngine.loadProducts();
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
