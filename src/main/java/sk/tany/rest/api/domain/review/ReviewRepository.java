package sk.tany.rest.api.domain.review;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findAllByProductId(String productId);

    List<Review> findAllByProductId(String productId, Sort sort);

    List<Review> findAllByProductIdIn(Collection<String> productIds);

    List<Review> findAllByProductIdIn(Collection<String> productIds, Sort sort);

    boolean existsByCustomerIdAndCustomerNameAndTitleAndText(String customerId, String customerName, String title, String text);
}
