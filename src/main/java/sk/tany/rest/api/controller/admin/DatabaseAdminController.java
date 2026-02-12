package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sk.tany.rest.api.service.admin.DatabaseAdminService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/admin/database")
@Tag(name = "Admin Database", description = "Endpoints for database management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class DatabaseAdminController {

    private final DatabaseAdminService service;

    @GetMapping("/export-json")
    @Operation(summary = "Export database to JSON zip")
    public ResponseEntity<Resource> exportDatabaseToJson() throws IOException {
        File file = service.exportDatabaseToJson();
        long contentLength = file.length();

        FileInputStream fis = new FileInputStream(file) {
            @Override
            public void close() throws IOException {
                super.close();
                if (file.exists() && !file.delete()) {
                    log.warn("Failed to delete temporary export file: {}", file.getAbsolutePath());
                }
            }
        };

        InputStreamResource resource = new InputStreamResource(fis);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"database_export.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(contentLength)
                .body(resource);
    }

    @PostMapping("/import-json")
    @Operation(summary = "Import database from JSON folder (server-side path)")
    public ResponseEntity<Void> importDatabaseFromJson(@RequestParam(defaultValue = "migration_data") String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return ResponseEntity.badRequest().build();
        }
        service.importDatabaseFromJson(folder);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/import-db", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import database from nitrite db file")
    public ResponseEntity<Void> importDatabase(@RequestParam("file") MultipartFile file) {
        service.importDatabase(file);
        return ResponseEntity.ok().build();
    }
}
