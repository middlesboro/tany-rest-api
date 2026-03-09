package sk.tany.rest.api.dto.admin.product.create;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductImportUrlAiResponse {
    @Description("The title or name of the product")
    private String title;

    @Description("A short summary or description of the product")
    private String shortDescription;

    @Description("The full, detailed description of the product, could contain HTML formatting if appropriate")
    private String description;

    @Description("The price of the product. If multiple prices exist, choose the main selling price. e.g. €10,50")
    private BigDecimal price;

    @Description("The weight of the product if available")
    private BigDecimal weight;

    @Description("The stock quantity if available")
    private Integer quantity;

    @Description("The meta title for SEO if available, or just the title")
    private String metaTitle;

    @Description("The meta description for SEO if available, or just a short description")
    private String metaDescription;

    @Description("The product code or SKU")
    private String productCode;

    @Description("The European Article Number (EAN) or barcode")
    private String ean;

    @Description("A list of image URLs associated with the product")
    private List<String> images;
}
