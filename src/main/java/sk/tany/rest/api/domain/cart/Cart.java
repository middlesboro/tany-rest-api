package sk.tany.rest.api.domain.cart;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "carts")
public class Cart {

    @Id
    private String cartId;
    private String customerId;
    private List<CartItem> items;
    @CreatedDate
    private Instant createDate;
    @LastModifiedDate
    private Instant updateDate;
}
