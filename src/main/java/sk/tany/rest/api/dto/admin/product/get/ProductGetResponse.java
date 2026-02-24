package sk.tany.rest.api.dto.admin.product.get;

import lombok.Data;
import sk.tany.rest.api.domain.product.ProductStatus;
import sk.tany.rest.api.dto.ProductFilterParameterDto;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductGetResponse {
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
    private List<String> categoryIds;
    private String supplierId;
    private String brandId;
    private List<String> images;
    private ProductStatus status;
    private Boolean active;
    private List<ProductFilterParameterDto> productFilterParameters;
    private List<String> productLabelIds;
    private BigDecimal discountValue;
    private BigDecimal discountPercentualValue;
    private BigDecimal discountPrice;
    private BigDecimal discountPriceWithoutVat;
    private boolean externalStock;
    private String defaultCategoryId;
}
