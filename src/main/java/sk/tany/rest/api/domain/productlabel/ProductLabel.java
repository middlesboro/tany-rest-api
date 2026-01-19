package sk.tany.rest.api.domain.productlabel;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "product_labels")
public class ProductLabel {
    @Id
    private String id;
    @CreatedDate
    private Instant createDate;
    @LastModifiedDate
    private Instant updateDate;
    private String color;
    private String title;
    private String productId;
    private ProductLabelPosition position;
}
