package sk.tany.rest.api.domain.carrier;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarrierPrice {
    private BigDecimal price;
    private BigDecimal weightFrom;
    private BigDecimal weightTo;
}
