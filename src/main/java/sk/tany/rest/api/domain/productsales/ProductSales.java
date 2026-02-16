package sk.tany.rest.api.domain.productsales;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class ProductSales extends BaseEntity {
private String productId;
    private Integer salesCount;
}
