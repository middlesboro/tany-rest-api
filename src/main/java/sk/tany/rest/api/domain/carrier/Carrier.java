package sk.tany.rest.api.domain.carrier;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class Carrier {

    private String id;
    private Long prestashopId;
    private String name;
    private CarrierType type;
    private BigDecimal wholesalePrice;
    private BigDecimal priceWithoutVat;
    private BigDecimal price;
    private String description;
    private String image;
    private List<CarrierPriceRange> prices;
    private boolean selected;
    private Instant createdDate;
    private Instant updateDate;

    public List<CarrierPriceRange> getRanges() {
        return prices;
    }
}
