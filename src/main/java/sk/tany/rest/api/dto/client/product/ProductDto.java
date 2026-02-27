package sk.tany.rest.api.dto.client.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private String id;
    private String title;
    private String image;
    private BigDecimal price;
    private BigDecimal discountPrice;
}
