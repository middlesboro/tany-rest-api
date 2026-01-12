package sk.tany.rest.api.dto;

import lombok.Data;
import sk.tany.rest.api.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductDto {
    private String id;
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
    private List<String> categoryIds = new ArrayList<>();
    private String supplierId;
    private String brandId;
    private List<String> images;
    private ProductStatus status;
    private BigDecimal averageRating;
    private Integer reviewsCount;
}
