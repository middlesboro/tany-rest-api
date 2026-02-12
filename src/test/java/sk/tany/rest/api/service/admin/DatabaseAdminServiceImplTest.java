package sk.tany.rest.api.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;
import sk.tany.rest.api.domain.BaseEntity;
import sk.tany.rest.api.service.admin.impl.DatabaseAdminServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseAdminServiceImplTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DatabaseAdminServiceImpl service;

    @Mock
    private AbstractInMemoryRepository mockRepo;

    @BeforeEach
    void setUp() {
        when(applicationContext.getBeansOfType(AbstractInMemoryRepository.class))
                .thenReturn(Map.of("mockRepo", mockRepo));
    }

    @Test
    void exportDatabaseToJson_ShouldCreateZip() throws IOException {
        // Setup repo
        when(mockRepo.findAll()).thenReturn(List.of(new TestEntity()));
        doReturn(TestEntity.class).when(mockRepo).getEntityType();

        // Act
        File result = service.exportDatabaseToJson();

        // Assert
        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.getName().endsWith(".zip"));

        // Verify interaction
        verify(mockRepo).findAll();
        verify(objectMapper).writeValue(any(File.class), any(List.class));

        result.delete();
    }

    @Test
    void importDatabaseFromJson_ShouldImport() throws IOException {
        // Prepare a dummy JSON file
        File tempDir = java.nio.file.Files.createTempDirectory("temp_import").toFile();

        File jsonFile = new File(tempDir, "TestEntity.json");
        jsonFile.createNewFile();

        doReturn(TestEntity.class).when(mockRepo).getEntityType();

        when(objectMapper.getTypeFactory()).thenReturn(com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance());
        // Mock readValue
        when(objectMapper.readValue(eq(jsonFile), any(com.fasterxml.jackson.databind.JavaType.class)))
                .thenReturn(List.of(new TestEntity()));

        // Act
        service.importDatabaseFromJson(tempDir);

        // Assert
        verify(mockRepo).deleteAll();
        verify(mockRepo).saveAll(any());

        // Cleanup
        jsonFile.delete();
        tempDir.delete();
    }

    // Dummy entity
    static class TestEntity implements BaseEntity {
        @Override public String getId() { return "1"; }
        @Override public void setId(String id) {}
        @Override public void setCreatedDate(java.time.Instant date) {}
        @Override public java.time.Instant getCreatedDate() { return null; }
        @Override public void setLastModifiedDate(java.time.Instant date) {}
        @Override public java.time.Instant getLastModifiedDate() { return null; }
    }
}
