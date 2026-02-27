package sk.tany.rest.api.dto;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CrossSellProductDto {

    @Description("The unique identifier of the product")
    private String id;

    @Description("The title or name of the product")
    private String title;

    @Description("The URL or path to the product image")
    private String image;

    @Description("The product price")
    private BigDecimal price;

    @Description("The URL slug for the product")
    private String slug;

}
