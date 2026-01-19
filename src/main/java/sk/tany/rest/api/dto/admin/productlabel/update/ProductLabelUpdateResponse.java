package sk.tany.rest.api.dto.admin.productlabel.update;

import lombok.Data;
import sk.tany.rest.api.domain.productlabel.ProductLabelPosition;

@Data
public class ProductLabelUpdateResponse {
    private String id;
    private String color;
    private String title;
    private String productId;
    private ProductLabelPosition position;
}
