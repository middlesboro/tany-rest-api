package sk.tany.rest.api.domain.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dizitart.no2.repository.annotations.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist implements BaseEntity {

    @Id
    private String id;
    private String customerId;
    private String productId;
    private Instant createdDate;

    @Override
    public void setLastModifiedDate(Instant date) { }
    @Override
    public Instant getLastModifiedDate() { return null; }
}
