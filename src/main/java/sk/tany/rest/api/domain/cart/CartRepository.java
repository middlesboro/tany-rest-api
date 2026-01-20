package sk.tany.rest.api.domain.cart;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

@Repository
public class CartRepository extends AbstractInMemoryRepository<Cart> {

    public CartRepository(Nitrite nitrite) {
        super(nitrite, Cart.class);
    }
}
