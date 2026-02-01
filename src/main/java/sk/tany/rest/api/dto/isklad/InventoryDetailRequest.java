package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDetailRequest {
    @JsonProperty("item_id_list")
    private List<Long> itemIdList;

    @JsonProperty("cached")
    private Integer cached;

    @JsonProperty("only_on_stock")
    private Integer onlyOnStock;
}
