package sk.tany.rest.api.dto.admin.product.create;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ProductImportUrlRequest {
    @NotBlank
    private String url;

    private String brandId;

    private String supplierId;
}
