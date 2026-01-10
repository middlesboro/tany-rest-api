package sk.tany.rest.api.dto.prestashop;

import lombok.Data;
import java.util.List;

@Data
public class PrestaShopCategoriesResponse {
    private List<PrestaShopCategoryResponse> categories;
}
