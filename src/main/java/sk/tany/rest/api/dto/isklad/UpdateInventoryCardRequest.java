package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInventoryCardRequest {

    @JsonProperty("item_id")
    private Long itemId;

    @JsonProperty("shop_setting_id")
    private Integer shopSettingId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("ean")
    private String ean;

    @JsonProperty("price_without_tax")
    private BigDecimal priceWithoutTax;

    @JsonProperty("mj")
    private String mj;

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("producer")
    private String producer;

    @JsonProperty("supplier")
    private String supplier;

    @JsonProperty("images")
    private List<String> images;
}
