package sk.tany.rest.api.dto.admin.import_product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductImportEntryDto {
    private String type;
    private String name;
    private List<ProductImportDataDto> data;
}
