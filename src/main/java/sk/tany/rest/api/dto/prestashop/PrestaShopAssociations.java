package sk.tany.rest.api.dto.prestashop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PrestaShopAssociations {
    @JsonProperty("categories")
    private List<PrestaShopCategory> categories;

    @JsonProperty("images")
    private List<PrestaShopImage> images;
}
