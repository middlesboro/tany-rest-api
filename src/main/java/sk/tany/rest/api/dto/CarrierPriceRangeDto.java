package sk.tany.rest.api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarrierPriceRangeDto {
    private BigDecimal price;
    private BigDecimal weightFrom;
    private BigDecimal weightTo;
}
