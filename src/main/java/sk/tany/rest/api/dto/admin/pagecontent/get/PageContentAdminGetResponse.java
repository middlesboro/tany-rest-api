package sk.tany.rest.api.dto.admin.pagecontent.get;

import lombok.Data;
import java.time.Instant;

@Data
public class PageContentAdminGetResponse {
    private String id;
    private String title;
    private String description;
    private String metaTitle;
    private String metaDescription;
    private String slug;
    private boolean visible;
    private Instant createDate;
    private Instant updateDate;
}
