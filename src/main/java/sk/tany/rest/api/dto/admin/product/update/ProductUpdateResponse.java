package sk.tany.rest.api.dto.admin.product.update;

import lombok.Data;
import sk.tany.rest.api.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateResponse {
    private String id;
    private Long productIdentifier;
    private String title;
    private String shortDescription;
    private String description;
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
    private BigDecimal discountValue;
    private BigDecimal discountPercentualValue;
    private BigDecimal discountPrice;
    private BigDecimal discountPriceWithoutVat;
    private BigDecimal wholesalePrice;
    private boolean externalStock;
    private String defaultCategoryId;
}
