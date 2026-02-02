package sk.tany.rest.api.dto.client.product.get;

import lombok.Data;
import sk.tany.rest.api.domain.product.ProductStatus;
import sk.tany.rest.api.dto.client.product.label.ProductLabelClientDto;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductClientGetResponse {
    private String id;
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
    private String defaultCategoryTitle;
    private String supplierId;
    private String brandId;
    private List<String> images;
    private ProductStatus status;
    private List<ProductLabelClientDto> productLabels;
    private Boolean inWishlist;
    private BigDecimal averageRating;
    private BigDecimal discountValue;
    private BigDecimal discountPercentualValue;
    private BigDecimal discountPrice;
    private boolean externalStock;
}
