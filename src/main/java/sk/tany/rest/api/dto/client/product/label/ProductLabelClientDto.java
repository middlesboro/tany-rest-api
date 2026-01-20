package sk.tany.rest.api.dto.client.product.label;

import lombok.Data;
import sk.tany.rest.api.domain.productlabel.ProductLabelPosition;

@Data
public class ProductLabelClientDto {
    private String color;
    private String backgroundColor;
    private String title;
    private ProductLabelPosition position;
}
