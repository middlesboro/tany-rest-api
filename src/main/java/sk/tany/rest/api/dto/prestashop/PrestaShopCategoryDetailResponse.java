package sk.tany.rest.api.dto.prestashop;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PrestaShopCategoryDetailResponse {
    private Long id;
    private Object name;
    private String active;
    @JsonProperty("id_parent")
    private Long idParent;
    private Object description;
    @JsonProperty("link_rewrite")
    private Object linkRewrite;
    @JsonProperty("meta_title")
    private Object metaTitle;
    @JsonProperty("meta_description")
    private Object metaDescription;
}
