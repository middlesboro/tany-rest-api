package sk.tany.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import sk.tany.rest.api.domain.carrier.CarrierType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class CarrierDto {
    private String id;
    private String name;
    private String description;
    private String image;
    private Integer order;
    private CarrierType type;
    private BigDecimal wholesalePrice;
    private BigDecimal priceWithoutVat;
    private BigDecimal price;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CarrierPriceRangeDto> ranges;
    private Instant createdDate;
    private Instant updateDate;
    private boolean selected;
}
