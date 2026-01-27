package sk.tany.rest.api.domain.cartdiscount;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CartDiscountRepository extends AbstractInMemoryRepository<CartDiscount> {

    public CartDiscountRepository(Nitrite nitrite) {
        super(nitrite, CartDiscount.class);
    }

    public Optional<CartDiscount> findByCode(String code) {
        return memoryCache.values().stream()
                .filter(cd -> cd.getCode() != null && cd.getCode().equals(code))
                .findFirst();
    }

    public boolean existsByCode(String code) {
        return memoryCache.values().stream()
                .anyMatch(cd -> cd.getCode() != null && cd.getCode().equals(code));
    }

    public Optional<CartDiscount> findByCodeAndActiveTrue(String code) {
        return memoryCache.values().stream()
                .filter(cd -> cd.getCode() != null && cd.getCode().equals(code) && cd.isActive())
                .findFirst();
    }

    public List<CartDiscount> findAllByCodeIsNullAndActiveTrue() {
        return memoryCache.values().stream()
                .filter(cd -> cd.getCode() == null && cd.isActive())
                .collect(Collectors.toList());
    }

    public List<CartDiscount> findAllByAutomaticTrueAndActiveTrue() {
        return memoryCache.values().stream()
                .filter(cd -> cd.isAutomatic() && cd.isActive())
                .collect(Collectors.toList());
    }
}
