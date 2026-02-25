package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ISkladItem {
    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("catalog_id")
    private String catalogId;

    private String name;

    private Integer count;

    private Integer expiration;

    @JsonProperty("exp_value")
    private String expValue;

    private BigDecimal price;

    @JsonProperty("price_with_tax")
    private BigDecimal priceWithTax;

    private BigDecimal tax;
}
