package sk.tany.rest.api.domain.productlabel;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
public class ProductLabel extends BaseEntity {
    private String title;
    private String color;
    private String backgroundColor;
    private ProductLabelPosition position;
    private boolean active;
}
