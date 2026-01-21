package sk.tany.rest.api.domain.carrier;

import lombok.Data;
import org.dizitart.no2.objects.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class Carrier {

    @Id
    private String id;
    private Long prestashopId;
    private String name;
    private CarrierType type;
    private BigDecimal wholesalePrice;
    private BigDecimal priceWithoutVat;
    private BigDecimal price;
    private Integer order;
    private String description;
    private String image;
    private List<CarrierPriceRange> ranges;
    private boolean selected;
    private Instant createdDate;
    private Instant updateDate;
}
