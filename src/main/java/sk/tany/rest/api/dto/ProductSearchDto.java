package sk.tany.rest.api.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class ProductSearchDto {
    private Page<ProductDto> products;
    private List<FilterParameterDto> filterParameters;
}
