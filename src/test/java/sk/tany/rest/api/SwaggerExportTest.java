package sk.tany.rest.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=testsecret12345678901234567890123456789012",
        "eshop.base-url=http://localhost:8080",
        "eshop.frontend-url=http://localhost:3000",
        "de.flapdoodle.mongodb.embedded.version=7.0.0",
        "imagekit.url-endpoint=https://ik.imagekit.io/test",
        "imagekit.public-key=public_test",
        "imagekit.private-key=private_test",
        "mailersend.api-token=test_token",
        "security.excluded-urls[0]=/v3/api-docs/**",
        "security.excluded-urls[1]=/swagger-ui/**"
})
public class SwaggerExportTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void generateSwaggerAndPostmanCollection() throws Exception {
        // 1. Fetch Swagger YAML
        byte[] swaggerContent = mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        Path resourcesDir = Path.of("src", "main", "resources");
        Path swaggerPath = resourcesDir.resolve("swagger.yaml");
        Path postmanPath = resourcesDir.resolve("tany-rest-api.postman_collection.json");

        // 2. Save Swagger YAML
        Files.write(swaggerPath, swaggerContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("Swagger YAML saved to: " + swaggerPath.toAbsolutePath());

        // 3. Convert to Postman Collection using npx openapi-to-postmanv2
        if (isNpxAvailable()) {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(
                    "npx", "-y", "openapi-to-postmanv2",
                    "-s", swaggerPath.toString(),
                    "-o", postmanPath.toString(),
                    "-p", "-O", "folderStrategy=Tags"
            );

            // Redirect output to files to avoid interfering with Surefire's streams
            Path targetDir = Path.of("target");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            processBuilder.redirectOutput(targetDir.resolve("npx-output.log").toFile());
            processBuilder.redirectError(targetDir.resolve("npx-error.log").toFile());

            Process process = processBuilder.start();
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);

            if (!finished) {
                process.destroy();
                throw new RuntimeException("Postman generation timed out");
            }

            if (process.exitValue() != 0) {
                throw new RuntimeException("Postman generation failed with exit code " + process.exitValue());
            }

            System.out.println("Postman collection saved to: " + postmanPath.toAbsolutePath());
        } else {
            System.err.println("npx not found. Skipping Postman collection generation.");
        }
    }

    private boolean isNpxAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("npx", "--version");
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process process = processBuilder.start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
