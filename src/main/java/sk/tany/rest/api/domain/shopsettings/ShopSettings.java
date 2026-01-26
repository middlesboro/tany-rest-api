package sk.tany.rest.api.domain.shopsettings;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import java.time.Instant;

@Data
public class ShopSettings {

    @Id
    private String id;
    private String key;
    private String value;
    private String defaultCountry;
    private Instant createdDate;
    private Instant updateDate;
}
