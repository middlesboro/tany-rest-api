package sk.tany.rest.api.domain.pagecontent;

import lombok.Data;
import java.time.Instant;

@Data
public class PageContent {

    private String id;
    private String title;
    private String slug;
    private String content;
    private Instant createdDate;
    private Instant updateDate;
}
