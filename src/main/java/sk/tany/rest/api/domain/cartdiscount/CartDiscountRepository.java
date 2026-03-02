package sk.tany.rest.api.domain.cartdiscount;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CartDiscountRepository extends MongoRepository<CartDiscount, String> {
    Optional<CartDiscount> findByCode(String code);

    boolean existsByCode(String code);

    Optional<CartDiscount> findByCodeAndActiveTrue(String code);

    List<CartDiscount> findAll();

    // Logic: Active, Automatic, and matches criteria
    // Since we can't easily express "collection intersection" in a derived query name that handles "empty collection in DB means apply to all",
    // we can either fetch all automatic ones and filter in memory (safest for migration to ensure logic preservation)
    // or try a complex @Query.
    // Given the previous implementation likely iterated over all, let's keep it simple by fetching all candidate discounts and filtering in Service or default method.
    // However, interface default methods can't access instance data easily unless we pass `findAll...` result.

    // Let's implement it as a default method that fetches all automatic discounts and filters them.

    default List<CartDiscount> findApplicableAutomaticDiscounts(Set<String> productIds, Set<String> categoryIds, Set<String> brandIds) {
        List<CartDiscount> candidates = findAll().stream()
                .filter(cd -> cd.getCode() == null || cd.getCode().isBlank())
                .toList();
        return candidates.stream().filter(d -> isApplicable(d, productIds, categoryIds, brandIds)).toList();
    }

    private boolean isApplicable(CartDiscount d, Set<String> productIds, Set<String> categoryIds, Set<String> brandIds) {
        // If discount has specific products, cart must contain at least one
        boolean productMatch = d.getProductIds() == null || d.getProductIds().isEmpty() ||
                               (productIds != null && d.getProductIds().stream().anyMatch(productIds::contains));

        boolean categoryMatch = d.getCategoryIds() == null || d.getCategoryIds().isEmpty() ||
                                (categoryIds != null && d.getCategoryIds().stream().anyMatch(categoryIds::contains));

        boolean brandMatch = d.getBrandIds() == null || d.getBrandIds().isEmpty() ||
                             (brandIds != null && d.getBrandIds().stream().anyMatch(brandIds::contains));

        return productMatch && categoryMatch && brandMatch;
    }
}
