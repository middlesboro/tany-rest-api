package sk.tany.rest.api.domain.supplier;

import lombok.Data;
import java.time.Instant;

@Data
public class Supplier {

    private String id;
    private Long prestashopId;
    private String name;
    private Instant createdDate;
    private Instant updateDate;
}
