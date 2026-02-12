package sk.tany.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class NitriteV3ToJsonExporterTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void exportToJson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Path exportDir = Paths.get("migration_data");
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        Map<String, AbstractInMemoryRepository> repositories = applicationContext.getBeansOfType(AbstractInMemoryRepository.class);

        for (Map.Entry<String, AbstractInMemoryRepository> entry : repositories.entrySet()) {
            String repoName = entry.getKey();
            AbstractInMemoryRepository repository = entry.getValue();

            // We need to access the type of entity handled by this repository
            // Since `findAll()` returns a List<T>, and T extends BaseEntity, we can rely on runtime type info from the list content
            // or just use the bean name as filename.

            List<?> allEntities = repository.findAll();
            if (allEntities.isEmpty()) {
                System.out.println("Skipping empty repository: " + repoName);
                continue;
            }

            // Get class name from first element
            String entityClassName = allEntities.get(0).getClass().getSimpleName();
            File file = exportDir.resolve(entityClassName + ".json").toFile();

            System.out.println("Exporting " + allEntities.size() + " entities from " + repoName + " to " + file.getAbsolutePath());
            objectMapper.writeValue(file, allEntities);
        }

        System.out.println("Export complete. Check 'migration_data' folder.");
    }
}