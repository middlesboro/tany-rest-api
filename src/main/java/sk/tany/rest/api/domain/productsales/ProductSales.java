package sk.tany.rest.api.domain.productsales;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import java.time.Instant;

@Data
public class ProductSales {

    @Id
    private String id;
    private String productId;
    private Integer salesCount;
    private Instant createDate;
    private Instant updateDate;
}
