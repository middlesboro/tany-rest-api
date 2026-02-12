package sk.tany.rest.api.service.admin;

import java.io.File;

public interface DatabaseAdminService {
    File exportDatabaseToJson();
    void importDatabaseFromJson(File directory);
}
