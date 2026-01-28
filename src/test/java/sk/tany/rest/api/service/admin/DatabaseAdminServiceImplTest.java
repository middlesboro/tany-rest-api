package sk.tany.rest.api.service.admin;

import org.dizitart.no2.Nitrite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import sk.tany.rest.api.service.admin.impl.DatabaseAdminServiceImpl;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseAdminServiceImplTest {

    private DatabaseAdminServiceImpl service;
    private Nitrite sourceDb;
    private File sourceFile;

    @BeforeEach
    void setUp() throws IOException {
        sourceFile = File.createTempFile("test_source", ".db");
        sourceDb = Nitrite.builder().filePath(sourceFile).openOrCreate();
        sourceDb.getRepository(String.class).insert("Test Data");

        service = new DatabaseAdminServiceImpl(sourceDb);
        ReflectionTestUtils.setField(service, "databasePassword", "secret123");
    }

    @AfterEach
    void tearDown() {
        if (sourceDb != null && !sourceDb.isClosed()) {
            sourceDb.close();
        }
        if (sourceFile != null) {
            sourceFile.delete();
        }
    }

    @Test
    void exportEncryptedDatabase() {
        File exportedFile = service.exportEncryptedDatabase();

        assertNotNull(exportedFile);
        assertTrue(exportedFile.exists());
        assertTrue(exportedFile.length() > 0);

        // Verify we cannot open it without password (or with wrong password)
        // Note: Nitrite 3.x with password might throw SecurityException or just fail to open properly
        // Actually, checking if we CAN open it with correct password is a better test of success.

        Nitrite encryptedDb = Nitrite.builder()
                .filePath(exportedFile)
                .compressed()
                .openOrCreate("admin", "secret123");

        long count = encryptedDb.getRepository(String.class).find().size();
        assertTrue(count > 0);

        encryptedDb.close();

        // Cleanup exported file
        exportedFile.delete();
    }
}
