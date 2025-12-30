package sk.tany.rest.api.domain.order;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    private String cartId;
    private BigDecimal finalPrice;
    private List<String> productIds;
    private String customerId;

}
