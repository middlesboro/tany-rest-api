package sk.tany.rest.api.domain.customer;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "customers")
public class Customer {

    @Id
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String cartId;
}
