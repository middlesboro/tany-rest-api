package sk.tany.rest.api.dto;

import lombok.Data;

@Data
public class CartQuantityChangeDto {
    private String productId;
    private String productName;
    private String productImage;
    private Integer requestedQuantity;
    private Integer currentQuantity;
}
