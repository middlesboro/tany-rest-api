package sk.tany.rest.api.service.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.tool.Exporter;
import org.dizitart.no2.tool.Importer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.service.admin.DatabaseAdminService;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseAdminServiceImpl implements DatabaseAdminService {

    private final Nitrite nitrite;

    @Value("${admin.database.password}")
    private String databasePassword;

    @Override
    public File exportEncryptedDatabase() {
        File encryptedDbFile = null;
        Nitrite encryptedDb = null;
        boolean success = false;

        try {
            // 1. Create new encrypted Nitrite DB file
            encryptedDbFile = File.createTempFile("tany_encrypted_", ".db");

            // Restrict permissions to owner only
            if (!encryptedDbFile.setReadable(true, true) || !encryptedDbFile.setWritable(true, true)) {
                log.warn("Could not restrict permissions on temporary DB file: {}", encryptedDbFile.getAbsolutePath());
            }

            // Delete the file so Nitrite creates it fresh (important for security initialization)
            if (encryptedDbFile.delete()) {
                log.info("Temporary DB file deleted to allow fresh creation: {}", encryptedDbFile.getAbsolutePath());
            }

            log.info("Creating new encrypted database at: {}", encryptedDbFile.getAbsolutePath());
            encryptedDb = Nitrite.builder()
                    .compressed()
                    .filePath(encryptedDbFile)
                    .openOrCreate("admin", databasePassword);

            // 2. Setup Piping
            // Use a large buffer for the pipe to improve performance (e.g. 64KB)
            PipedInputStream pipedIn = new PipedInputStream(65536);
            PipedOutputStream pipedOut = new PipedOutputStream(pipedIn);

            // 3. Start Export Asynchronously
            CompletableFuture<Void> exportFuture = CompletableFuture.runAsync(() -> {
                try (pipedOut) { // Ensure stream is closed to signal EOF to reader
                    log.info("Starting export to pipe...");
                    Exporter.of(nitrite).exportTo(pipedOut);
                    log.info("Export to pipe finished.");
                } catch (IOException e) {
                    log.error("Export to pipe failed", e);
                    throw new CompletionException(e);
                }
            });

            // 4. Run Import Synchronously
            log.info("Starting import from pipe...");
            Importer.of(encryptedDb).importFrom(pipedIn);
            log.info("Import from pipe finished.");

            // 5. Check if export failed
            exportFuture.join();

            // Commit changes
            encryptedDb.commit();
            success = true;

            return encryptedDbFile;

        } catch (IOException | CompletionException e) {
            log.error("Error during database export", e);
            throw new RuntimeException("Failed to export database", e);
        } finally {
            // Cleanup
            if (encryptedDb != null) {
                encryptedDb.close();
            }
            // If failed, delete the partial/corrupt file
            if (!success && encryptedDbFile != null && encryptedDbFile.exists()) {
                if (!encryptedDbFile.delete()) {
                    log.warn("Failed to delete temporary DB file after error: {}", encryptedDbFile.getAbsolutePath());
                }
            }
        }
    }
}
