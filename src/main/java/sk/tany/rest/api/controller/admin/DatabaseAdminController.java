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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/export")
    @Operation(summary = "Export encrypted database")
    public ResponseEntity<Resource> exportEncryptedDatabase() throws IOException {
        File file = service.exportEncryptedDatabase();
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tany_encrypted.db\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(contentLength)
                .body(resource);
    }
}
