package sk.tany.rest.api.dto.prestashop;

import lombok.Data;

@Data
public class PrestaShopCategoryResponse {
    private Long id;
    private String name;
    private String active;
}
