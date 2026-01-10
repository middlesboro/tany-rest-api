package sk.tany.rest.api.dto.prestashop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PrestaShopManufacturerWrapper {
    @JsonProperty("manufacturer")
    private PrestaShopManufacturerDetailResponse manufacturer;
}
