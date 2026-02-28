package sk.tany.features.dto;

import lombok.Data;




import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductClientDto {
    private String id;
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
    private List<String> categoryIds = new ArrayList<>();
    private String defaultCategoryId;
    private String supplierId;
    private String brandId;
    private List<String> images = new ArrayList<>();
    private ProductStatus status;
    private Boolean active;
    private BigDecimal averageRating;
    private Integer reviewsCount;
    private Boolean inWishlist;
    private BigDecimal discountValue;
    private BigDecimal discountPercentualValue;
    private BigDecimal discountPrice;
    private BigDecimal discountPriceWithoutVat;
    private boolean externalStock;
}
