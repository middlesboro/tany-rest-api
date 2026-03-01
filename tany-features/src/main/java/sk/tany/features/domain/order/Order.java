package sk.tany.features.domain.order;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private Long orderIdentifier;
    private String email;
    private String phone;
    private String status;
    private String carrierOrderStateLink;
}
