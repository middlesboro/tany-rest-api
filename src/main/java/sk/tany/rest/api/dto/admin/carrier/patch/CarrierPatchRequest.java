package sk.tany.rest.api.dto.admin.carrier.patch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import sk.tany.rest.api.domain.carrier.CarrierType;
import sk.tany.rest.api.dto.CarrierPriceRangeDto;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CarrierPatchRequest {
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
}
