package sk.tany.rest.api.domain.pagecontent;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import java.time.Instant;

@Data
public class PageContent {

    @Id
    private String id;
    private String title;
    private String slug;
    private String content;
    private Instant createdDate;
    private Instant updateDate;
}
