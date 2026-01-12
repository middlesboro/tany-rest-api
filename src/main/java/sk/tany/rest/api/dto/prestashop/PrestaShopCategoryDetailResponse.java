package sk.tany.rest.api.dto.prestashop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PrestaShopCategoryDetailResponse {
    private Long id;
    private Object name;
    private Object description;
    private String active;
    @JsonProperty("is_visible")
    private String visible;
    private String position;
    @JsonProperty("id_parent")
    private String idParent;
    @JsonProperty("meta_title")
    private Object metaTitle;
    @JsonProperty("meta_description")
    private Object metaDescription;
    @JsonProperty("link_rewrite")
    private Object linkRewrite;
}
