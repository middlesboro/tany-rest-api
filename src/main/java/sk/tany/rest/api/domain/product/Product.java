package sk.tany.rest.api.domain.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class Product {

    private String id;
    private Long prestashopId;
    private Instant createDate;
    private Instant updateDate;
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
    private String supplierId;
    private String brandId;
    private List<String> images;
    private ProductStatus status;
    private boolean active = true;
    private BigDecimal averageRating;
    private Integer reviewsCount;
    private List<ProductFilterParameter> productFilterParameters;
    private List<String> productLabelIds;

}
