# Migration to Nitrite 4.x

This document describes the migration process from Nitrite 3.x to Nitrite 4.x.

## 1. Export Current Data (Before Upgrade)

Since Nitrite 4.x uses a different storage format and is not backward compatible with 3.x database files, you must export your data to JSON *before* upgrading the dependency.

### Step 1.1: Create Export Script

Create a temporary test file `src/test/java/sk/tany/rest/api/migration/NitriteV3ToJsonExporterTest.java` with the following content:

```java
package sk.tany.rest.api.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;
import sk.tany.rest.api.domain.BaseEntity;

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
```

### Step 1.2: Run the Export

Run this test using your IDE or Maven:
`mvn test -Dtest=NitriteV3ToJsonExporterTest`

Verify that the `migration_data` folder is created and contains JSON files for all your entities (e.g., `Product.json`, `Order.json`, etc.).

## 2. Upgrade to Nitrite 4.x

After exporting data, pull the changes that upgrade the project to Nitrite 4.x.
This will update `pom.xml` and the codebase.

## 3. Import Data (After Upgrade)

Once the application is running with Nitrite 4.x, use the new Import API to restore your data.

You can use the provided endpoint `/api/admin/database/import-json` (POST) which accepts a directory path or a zip file (implementation dependent).

Alternatively, copy the JSON files from `migration_data` to a location accessible by the server and trigger the import via the Admin Controller.

If running locally, you can use a similar test approach to import data if needed, or use the `DatabaseAdminService` directly.
