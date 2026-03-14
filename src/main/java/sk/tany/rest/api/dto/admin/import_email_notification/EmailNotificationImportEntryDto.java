package sk.tany.rest.api.dto.admin.import_email_notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailNotificationImportEntryDto {
    private String type;
    private String name;
    private List<EmailNotificationImportDataDto> data;
}
