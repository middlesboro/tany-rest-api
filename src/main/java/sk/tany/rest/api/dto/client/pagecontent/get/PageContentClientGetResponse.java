package sk.tany.rest.api.dto.client.pagecontent.get;

import lombok.Data;
import java.time.Instant;

@Data
public class PageContentClientGetResponse {
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
