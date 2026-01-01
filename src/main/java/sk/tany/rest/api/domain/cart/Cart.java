package sk.tany.rest.api.domain.cart;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = "carts")
public class Cart {

    @Id
    private String cartId;
    private String customerId;
    private Map<String, Integer> products;
}
