package sk.tany.rest.api.service.admin;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface DatabaseAdminService {
    File exportDatabaseToJson();
    void importDatabaseFromJson(File directory);
    void importDatabase(MultipartFile file);
}
