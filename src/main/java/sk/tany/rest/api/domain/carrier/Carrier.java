package sk.tany.rest.api.domain.carrier;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;
import java.util.List;

@Data
public class Carrier extends BaseEntity {
    private Long prestashopId;
    private Integer iskladId;
    private String name;
    private CarrierType type;
    private Integer order;
    private String description;
    private String image;
    private List<CarrierPriceRange> ranges;
    private boolean selected;
    @Override
    public Object getSortValue(String field) {
        if ("order".equals(field)) return order;
        if ("name".equals(field)) return name;
        return super.getSortValue(field);
    }
}
