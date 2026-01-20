package sk.tany.rest.api.domain.cartdiscount;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartDiscountRepository extends MongoRepository<CartDiscount, String> {
    Optional<CartDiscount> findByCode(String code);

    // Find automatic discounts (code is null) that are active and within date range (or no date range)
    // Complex queries might be better handled with criteria or filtering in service if dataset is small,
    // but let's try a query method.
    // However, since we need to check active=true, and date ranges.

    List<CartDiscount> findAllByCodeIsNullAndActiveTrue();

    // For manual code check
    Optional<CartDiscount> findByCodeAndActiveTrue(String code);

    boolean existsByCode(String code);
}
