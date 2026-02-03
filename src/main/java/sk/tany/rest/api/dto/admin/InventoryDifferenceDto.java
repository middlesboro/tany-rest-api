package sk.tany.rest.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDifferenceDto {
    private String productId;
    private String productName;
    private Integer dbQuantity;
    private Integer iskladQuantity;
}
