package sk.tany.rest.api.dto.admin.import_product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductImportDataDto {

    @JsonProperty("id_product")
    private String idProduct;
    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty("ean")
    private String ean;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("full_description")
    private String fullDescription;
    @JsonProperty("short_description")
    private String shortDescription;
    @JsonProperty("label_status")
    private String labelStatus;
    @JsonProperty("label_color")
    private String labelColor;
    @JsonProperty("label_background_color")
    private String labelBackgroundColor;
    @JsonProperty("label_text")
    private String labelText;
    @JsonProperty("brand_name")
    private String brandName;
    @JsonProperty("supplier_name")
    private String supplierName;
    @JsonProperty("price_tax_excl")
    private String priceTaxExcl;
    @JsonProperty("price_tax_incl")
    private String priceTaxIncl;
    @JsonProperty("wholesale_price")
    private String wholesalePrice;
    @JsonProperty("weight")
    private String weight;
    @JsonProperty("stock_qty")
    private String stockQty;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("filter_parameter")
    private String filterParameter;
    @JsonProperty("filter_parameter_value")
    private String filterParameterValue;
    @JsonProperty("image_id")
    private String imageId;
    @JsonProperty("is_cover")
    private String isCover;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("sold_quantity")
    private Integer soldQuantity;
}
