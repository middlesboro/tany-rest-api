package sk.tany.rest.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CrossSellProductDto {
    private String id;
    private String title;
    private String image;
    private BigDecimal price;
    private String slug;
}
