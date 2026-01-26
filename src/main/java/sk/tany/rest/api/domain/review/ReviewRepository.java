package sk.tany.rest.api.domain.review;

import org.dizitart.no2.Nitrite;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public List<Review> findAllByProductId(String productId, Sort sort) {
        List<Review> reviews = findAllByProductId(productId);
        sort(reviews, sort);
        return reviews;
    }

    public List<Review> findAllByProductIds(Collection<String> productIds) {
        Set<String> idsSet = new HashSet<>(productIds);
        return memoryCache.values().stream()
                .filter(r -> r.getProductId() != null && idsSet.contains(r.getProductId()))
                .collect(Collectors.toList());
    }
}
