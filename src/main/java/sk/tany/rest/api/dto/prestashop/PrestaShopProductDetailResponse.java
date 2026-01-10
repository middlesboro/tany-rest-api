package sk.tany.rest.api.dto.prestashop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrestaShopProductDetailResponse {
    private Long id;

    @JsonProperty("name")
    private Object name; // Can be String or List

    @JsonProperty("description")
    private Object description;

    @JsonProperty("description_short")
    private Object descriptionShort;

    private BigDecimal price;
    @JsonProperty("wholesale_price")
    private BigDecimal wholesalePrice;

    @JsonProperty("active")
    private String active; // "1" or "0"

    @JsonProperty("id_category_default")
    private String categoryIdDefault;

    @JsonProperty("associations")
    private PrestaShopAssociations associations;

    @JsonProperty("weight")
    private BigDecimal weight;

    @JsonProperty("reference") // productCode
    private String reference;

    @JsonProperty("ean13")
    private String ean13;

    @JsonProperty("link_rewrite") // slug
    private Object linkRewrite;
}
