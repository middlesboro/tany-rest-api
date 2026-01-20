package sk.tany.rest.api.domain.productsales;

import lombok.Data;
import java.time.Instant;

@Data
public class ProductSales {

    private String id;
    private String productId;
    private Integer salesCount;
    private Instant createDate;
    private Instant updateDate;
}
