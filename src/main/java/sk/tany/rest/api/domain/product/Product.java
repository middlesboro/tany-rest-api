package sk.tany.rest.api.domain.product;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import sk.tany.rest.api.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class Product extends BaseEntity {
    @Indexed(unique = true)
    private Long productIdentifier;
    private String title;
    private String shortDescription;
    private String description;
    private BigDecimal wholesalePrice;
    private BigDecimal priceWithoutVat;
    private BigDecimal price;
    private BigDecimal weight;
    private Integer quantity;
    private String metaTitle;
    private String metaDescription;
    private String productCode;
    private String ean;
    private String slug;
    private List<String> categoryIds;
    private String defaultCategoryId;
    private String supplierId;
    private String brandId;
    private List<String> images;
    private ProductStatus status;
    private boolean active = true;
    private boolean externalStock;
    private BigDecimal averageRating;
    private Integer reviewsCount;
    private List<ProductFilterParameter> productFilterParameters;
    private List<String> productLabelIds;
    private BigDecimal discountValue;
    private BigDecimal discountPercentualValue;
    private BigDecimal discountPrice;
    private BigDecimal discountPriceWithoutVat;
    @Override
    public Object getSortValue(String field) {
        switch (field) {
            case "title": return title;
            case "price": return price;
            case "active": return active;
            case "quantity": return quantity;
            case "productIdentifier": return productIdentifier;
            default: return super.getSortValue(field);
        }
    }
}
