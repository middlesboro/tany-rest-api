package sk.tany.rest.api.domain.review;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ReviewRepository extends AbstractInMemoryRepository<Review> {

    public ReviewRepository(Nitrite nitrite) {
        super(nitrite, Review.class);
    }

    public List<Review> findAllByProductId(String productId) {
        return memoryCache.values().stream()
                .filter(r -> r.getProductId() != null && r.getProductId().equals(productId))
                .collect(Collectors.toList());
    }
}
