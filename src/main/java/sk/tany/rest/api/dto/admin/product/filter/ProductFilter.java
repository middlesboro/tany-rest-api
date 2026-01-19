package sk.tany.rest.api.dto.admin.product.filter;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductFilter {
    private String query;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private String brandId;
    private String id;
    private Boolean externalStock;
    private Integer quantity;
    private Boolean active;
}
