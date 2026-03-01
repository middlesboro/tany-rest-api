package sk.tany.rest.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CrossSellProductDto {

    private String id;
    private String title;
    private String image;
    private BigDecimal price;
    private String slug;

}
