package sk.tany.rest.api.domain.product;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "products")
public class Product {

    @Id
    private String id;
    @CreatedDate
    private Instant createDate;
    @LastModifiedDate
    private Instant updateDate;
    private String title;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String metaTitle;
    private String metaDescription;
    private String productCode;
    private String ean;
    private String slug;
    private List<String> categoryIds;
    private List<String> images;

}
