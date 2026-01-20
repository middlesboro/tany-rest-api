package sk.tany.rest.api.dto.client.product;

import lombok.Data;
import org.springframework.data.domain.Page;
import sk.tany.rest.api.dto.FilterParameterDto;

import java.util.List;

@Data
public class ProductClientSearchDto {
    private Page<ProductClientDto> products;
    private List<FilterParameterDto> filterParameters;
}
