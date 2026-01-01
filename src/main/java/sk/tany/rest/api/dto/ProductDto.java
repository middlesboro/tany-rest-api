package sk.tany.rest.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    private String id;
    private String title;
    private String shortDescription;
    private String description;
    private BigDecimal price;
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
}
