package sk.tany.features.domain.product;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String title;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer quantity;
    private String slug;
    private List<String> images;
    private Boolean active;
    private BigDecimal averageRating;
    private Integer reviewsCount;
}
