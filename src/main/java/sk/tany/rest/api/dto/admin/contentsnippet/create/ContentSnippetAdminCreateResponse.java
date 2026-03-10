package sk.tany.rest.api.dto.admin.contentsnippet.create;

import lombok.Data;
import java.time.Instant;

@Data
public class ContentSnippetAdminCreateResponse {
    private String id;
    private String name;
    private String placeholder;
    private String content;
    private Instant createDate;
    private Instant updateDate;
}
