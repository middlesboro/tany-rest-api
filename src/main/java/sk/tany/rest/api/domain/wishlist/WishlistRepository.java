package sk.tany.rest.api.domain.wishlist;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class WishlistRepository extends AbstractInMemoryRepository<Wishlist> {

    public WishlistRepository(Nitrite nitrite) {
        super(nitrite, Wishlist.class);
    }

    public List<Wishlist> findByCustomerId(String customerId) {
        return memoryCache.values().stream()
                .filter(w -> w.getCustomerId() != null && w.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    public Optional<Wishlist> findByCustomerIdAndProductId(String customerId, String productId) {
        return memoryCache.values().stream()
                .filter(w -> w.getCustomerId() != null && w.getCustomerId().equals(customerId)
                        && w.getProductId() != null && w.getProductId().equals(productId))
                .findFirst();
    }

    public java.util.List<Wishlist> findAllItems() {
        return new java.util.ArrayList<>(memoryCache.values());
    }
}
