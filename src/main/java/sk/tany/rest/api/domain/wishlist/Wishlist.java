package sk.tany.rest.api.domain.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist extends BaseEntity {
    private String customerId;
    private String productId;
}
