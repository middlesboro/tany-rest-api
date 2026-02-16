package sk.tany.rest.api.domain.wishlist;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {
    List<Wishlist> findByCustomerId(String customerId);

    Optional<Wishlist> findByCustomerIdAndProductId(String customerId, String productId);
}
