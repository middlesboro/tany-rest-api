package sk.tany.rest.api.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.dto.OneDriveFileDto;
import sk.tany.rest.api.service.OneDriveService;
import sk.tany.rest.api.service.admin.DatabaseAdminService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseExportScheduler {

    private final DatabaseAdminService databaseAdminService;
    private final OneDriveService oneDriveService;

    private static final String EXPORT_FOLDER = "tany/db";
    private static final String FILENAME_PREFIX = "tany.db_";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Scheduled(cron = "0 */30 * * * *")
    public void exportDatabase() {
        log.info("Starting scheduled database export to OneDrive...");
        File exportedFile = null;
        try {
            // 1. Export database
            exportedFile = databaseAdminService.exportDatabaseToJson();
            if (exportedFile == null || !exportedFile.exists()) {
                log.error("Database export failed. File is null or does not exist.");
                return;
            }

            // 2. Prepare filename
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String filename = FILENAME_PREFIX + timestamp + ".zip";

            // 3. Upload to OneDrive
            byte[] content = Files.readAllBytes(exportedFile.toPath());
            // oneDriveService.uploadFile expects filename. Does it handle extension?
            // Assuming filename is full name.
            oneDriveService.uploadFile(EXPORT_FOLDER, filename, content);
            log.info("Database exported successfully to OneDrive: {}/{}", EXPORT_FOLDER, filename);

            // 4. Cleanup local file
            if (exportedFile.delete()) {
                log.debug("Local temporary export file deleted: {}", exportedFile.getAbsolutePath());
            } else {
                log.warn("Failed to delete local temporary export file: {}", exportedFile.getAbsolutePath());
            }

            // 5. Rotate backups (keep last 10)
            rotateBackups();

        } catch (IOException e) {
            log.error("Failed to read exported file content", e);
        } catch (Exception e) {
            log.error("Error during scheduled database export", e);
        } finally {
            // Ensure cleanup in case of error if not already done
            if (exportedFile != null && exportedFile.exists()) {
                if (!exportedFile.delete()) {
                    log.warn("Failed to delete local temporary export file in finally block: {}", exportedFile.getAbsolutePath());
                }
            }
        }
    }

    private void rotateBackups() {
        try {
            List<OneDriveFileDto> files = oneDriveService.listFiles(EXPORT_FOLDER);

            // Filter strictly for our files
            List<OneDriveFileDto> exportFiles = files.stream()
                    .filter(f -> f.getName() != null && f.getName().startsWith(FILENAME_PREFIX))
                    .sorted(Comparator.comparing(OneDriveFileDto::getCreatedDateTime).reversed())
                    .collect(Collectors.toList());

            if (exportFiles.size() > 10) {
                List<OneDriveFileDto> filesToDelete = exportFiles.subList(10, exportFiles.size());
                log.info("Found {} old database exports to remove.", filesToDelete.size());

                for (OneDriveFileDto file : filesToDelete) {
                    oneDriveService.deleteFile(file.getId());
                    log.debug("Removed old export: {}", file.getName());
                }
            }
        } catch (Exception e) {
            log.error("Error during backup rotation", e);
        }
    }
}
