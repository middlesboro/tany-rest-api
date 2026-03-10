package sk.tany.rest.api.dto.admin.contentsnippet.update;

import lombok.Data;
import java.time.Instant;

@Data
public class ContentSnippetAdminUpdateResponse {
    private String id;
    private String name;
    private String placeholder;
    private String content;
    private Instant createDate;
    private Instant updateDate;
}
