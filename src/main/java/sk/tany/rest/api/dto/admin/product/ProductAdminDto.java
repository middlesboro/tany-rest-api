package sk.tany.rest.api.dto.admin.product;

import lombok.Data;
import sk.tany.rest.api.domain.product.ProductStatus;
import sk.tany.rest.api.dto.ProductFilterParameterDto;
import sk.tany.rest.api.dto.ProductLabelDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductAdminDto {
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
    private String supplierId;
    private String brandId;
    private List<String> images = new ArrayList<>();
    private ProductStatus status;
    private Boolean active;
    private BigDecimal averageRating;
    private Integer reviewsCount;
    private List<ProductFilterParameterDto> productFilterParameters = new ArrayList<>();
    private List<ProductLabelDto> productLabels = new ArrayList<>();
    private BigDecimal discountValue;
    private BigDecimal discountPercentualValue;
    private BigDecimal discountPrice;
    private BigDecimal discountPriceWithoutVat;
    private boolean externalStock;
    private String defaultCategoryId;
}
