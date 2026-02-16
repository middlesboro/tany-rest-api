package sk.tany.rest.api.dto.client.product.search;

import lombok.Data;
import org.springframework.data.domain.Page;
import sk.tany.rest.api.dto.FilterParameterDto;
import sk.tany.rest.api.dto.client.product.list.ProductClientListResponse;

import java.util.List;

@Data
public class ProductClientSearchResponse {
    private Page<ProductClientListResponse> products;
    private List<FilterParameterDto> filterParameters;
    private String metaTitle;
    private String metaDescription;
}
