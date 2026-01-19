package sk.tany.rest.api.dto.admin.product.update;

import lombok.Data;
import sk.tany.rest.api.domain.product.ProductStatus;
import sk.tany.rest.api.dto.ProductFilterParameterDto;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateRequest {
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
    private Boolean active;
    private List<ProductFilterParameterDto> productFilterParameters;
}
