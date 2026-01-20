package sk.tany.rest.api.domain.blog;

import lombok.Data;
import java.time.Instant;

@Data
public class Blog {

    private String id;
    private String title;
    private String slug;
    private String shortDescription;
    private String content;
    private String image;
    private boolean visible;
    private Instant createdDate;
    private Instant updateDate;
}
