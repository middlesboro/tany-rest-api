package sk.tany.rest.api.dto.admin.pagecontent.list;

import lombok.Data;
import java.time.Instant;

@Data
public class PageContentAdminListResponse {
    private String id;
    private String title;
    private String slug;
    private boolean visible;
    private Instant createdDate;
    private Instant updateDate;
}
