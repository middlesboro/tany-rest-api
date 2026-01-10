package sk.tany.rest.api.dto.prestashop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PrestaShopManufacturerDetailResponse {
    private Long id;
    private String name;
    private String active;
    @JsonProperty("meta_title")
    private Object metaTitle;
    @JsonProperty("meta_description")
    private Object metaDescription;
}
