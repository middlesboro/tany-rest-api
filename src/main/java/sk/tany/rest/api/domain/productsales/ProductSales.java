package sk.tany.rest.api.domain.productsales;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "product_sales")
public class ProductSales {

    @Id
    private String id;

    @Indexed(unique = true)
    private String productId;

    private Integer salesCount;

    @CreatedDate
    private Instant createDate;

    @LastModifiedDate
    private Instant updateDate;
}
