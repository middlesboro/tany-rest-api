package sk.tany.rest.api.dto.admin.product.create;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class ProductImportImagesRequest {
    @NotEmpty
    private List<String> urls;
}
