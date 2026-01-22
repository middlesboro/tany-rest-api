package sk.tany.rest.api.domain.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.objects.Id;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    @Id
    private String id;
    private String customerId;
    private String productId;
    private Instant createdDate;
}
