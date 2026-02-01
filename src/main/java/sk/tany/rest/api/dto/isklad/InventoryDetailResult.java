package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class InventoryDetailResult {

    private Boolean success;

    @JsonProperty("inventory_details")
    private Map<String, InventoryDetailItem> inventoryDetails;

    @Data
    @NoArgsConstructor
    public static class InventoryDetailItem {
        private Long id;

        @JsonProperty("item_id")
        private Long itemId;

        @JsonProperty("catalog_id")
        private String catalogId;

        private String ean;
        private String name;

        @JsonProperty("count_types")
        private CountTypes countTypes;
    }

    @Data
    @NoArgsConstructor
    public static class CountTypes {
        private Integer all;
        private Integer expired;
        private Integer damaged;
        private Integer ordered;
        private Integer reserved;

        @JsonProperty("expirationBlocked")
        private Integer expirationBlocked;
    }
}
