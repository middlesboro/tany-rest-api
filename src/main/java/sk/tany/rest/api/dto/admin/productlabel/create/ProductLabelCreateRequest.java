package sk.tany.rest.api.dto.admin.productlabel.create;

import lombok.Data;
import sk.tany.rest.api.domain.productlabel.ProductLabelPosition;

@Data
public class ProductLabelCreateRequest {
    private String color;
    private String title;
    private String productId;
    private ProductLabelPosition position;
}
