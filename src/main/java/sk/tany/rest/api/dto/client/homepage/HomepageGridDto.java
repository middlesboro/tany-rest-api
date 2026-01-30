package sk.tany.rest.api.dto.client.homepage;

import lombok.Data;
import sk.tany.rest.api.dto.client.product.ProductClientDto;

import java.util.List;

@Data
public class HomepageGridDto {
    private String id;
    private String title;
    private List<ProductClientDto> products;
}
