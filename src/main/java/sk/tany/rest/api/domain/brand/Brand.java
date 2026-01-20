package sk.tany.rest.api.domain.brand;

import lombok.Data;
import java.time.Instant;

@Data
public class Brand {
    private String id;
    private Long prestashopId;
    private String name;
    private String image;
    private String metaTitle;
    private boolean active;
    private String metaDescription;
    private Instant createdDate;
    private Instant updateDate;
}
