package sk.tany.rest.api.service.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.dto.OneDriveFileDto;
import sk.tany.rest.api.service.OneDriveService;
import sk.tany.rest.api.service.admin.DatabaseAdminService;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseExportSchedulerTest {

    @Mock
    private DatabaseAdminService databaseAdminService;

    @Mock
    private OneDriveService oneDriveService;

    @InjectMocks
    private DatabaseExportScheduler scheduler;

    @Test
    void exportDatabase_ExportFails_DoesNothing() throws IOException {
        when(databaseAdminService.exportDatabaseToJson()).thenReturn(null);

        scheduler.exportDatabase();

        verify(oneDriveService, never()).uploadFile(anyString(), anyString(), any());
    }

    @Test
    void exportDatabase_Success_UploadsAndRotates() throws IOException {
        File tempFile = File.createTempFile("test_export", ".zip");
        // Write something to file so readAllBytes doesn't fail or return empty
        // Actually readAllBytes works on empty file too.

        when(databaseAdminService.exportDatabaseToJson()).thenReturn(tempFile);

        // Setup rotation mock
        List<OneDriveFileDto> files = new ArrayList<>();
        // Create 15 files.
        // Logic sorts by CreatedDateTime DESC (newest first).
        // Sublist(10, size) deletes oldest.
        for (int i = 0; i < 15; i++) {
            files.add(OneDriveFileDto.builder()
                    .id("id" + i)
                    .name("tany.db_backup_" + i)
                    .createdDateTime(OffsetDateTime.now().minusMinutes(i)) // id0 is newest, id14 is oldest
                    .build());
        }

        when(oneDriveService.listFiles("tany/db")).thenReturn(files);

        scheduler.exportDatabase();

        verify(oneDriveService).uploadFile(eq("tany/db"), startsWith("tany.db_"), any());

        // Should delete 5 oldest (id10 to id14)
        verify(oneDriveService, times(5)).deleteFile(anyString());
        verify(oneDriveService).deleteFile("id14");
    }
}
