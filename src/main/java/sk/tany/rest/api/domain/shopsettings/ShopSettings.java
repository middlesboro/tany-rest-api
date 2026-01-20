package sk.tany.rest.api.domain.shopsettings;

import lombok.Data;
import java.time.Instant;

@Data
public class ShopSettings {

    private String id;
    private String key;
    private String value;
    private Instant createdDate;
    private Instant updateDate;
}
