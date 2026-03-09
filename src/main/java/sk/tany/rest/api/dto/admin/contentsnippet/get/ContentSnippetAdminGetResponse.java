package sk.tany.rest.api.dto.admin.contentsnippet.get;

import lombok.Data;
import java.time.Instant;

@Data
public class ContentSnippetAdminGetResponse {
    private String id;
    private String name;
    private String placeholder;
    private String content;
    private Instant createDate;
    private Instant updateDate;
}
