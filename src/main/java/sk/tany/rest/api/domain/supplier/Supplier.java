package sk.tany.rest.api.domain.supplier;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import java.time.Instant;

@Data
public class Supplier {

    @Id
    private String id;
    private Long prestashopId;
    private String name;
    private Instant createdDate;
    private Instant updateDate;
}
