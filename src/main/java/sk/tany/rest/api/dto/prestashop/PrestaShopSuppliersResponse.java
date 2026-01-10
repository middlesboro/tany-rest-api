package sk.tany.rest.api.dto.prestashop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PrestaShopSuppliersResponse {
    @JsonProperty("suppliers")
    private List<PrestaShopSupplierResponse> suppliers;
}
