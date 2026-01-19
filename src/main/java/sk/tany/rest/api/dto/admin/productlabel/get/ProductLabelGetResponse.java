package sk.tany.rest.api.dto.admin.productlabel.get;

import lombok.Data;
import sk.tany.rest.api.domain.productlabel.ProductLabelPosition;

@Data
public class ProductLabelGetResponse {
    private String id;
    private String color;
    private String backgroundColor;
    private String title;
    private String productId;
    private ProductLabelPosition position;
    private boolean active;
}
