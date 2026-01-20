package sk.tany.rest.api.domain.productlabel;

import lombok.Data;
import java.time.Instant;

@Data
public class ProductLabel {
    private String id;
    private String title;
    private String color;
    private String backgroundColor;
    private ProductLabelPosition position;
    private boolean active;
    private Instant createDate;
    private Instant updateDate;
}
