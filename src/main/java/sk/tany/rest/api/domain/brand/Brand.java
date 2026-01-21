package sk.tany.rest.api.domain.brand;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import java.time.Instant;

@Data
public class Brand {
    @Id
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
