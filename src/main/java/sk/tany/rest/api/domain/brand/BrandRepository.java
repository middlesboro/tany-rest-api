package sk.tany.rest.api.domain.brand;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class BrandRepository extends AbstractInMemoryRepository<Brand> {

    public BrandRepository(Nitrite nitrite) {
        super(nitrite, Brand.class);
    }

    public Optional<Brand> findByPrestashopId(Long prestashopId) {
        return memoryCache.values().stream()
                .filter(b -> b.getPrestashopId() != null && b.getPrestashopId().equals(prestashopId))
                .findFirst();
    }

    public Optional<Brand> findByName(String name) {
        return memoryCache.values().stream()
                .filter(b -> b.getName() != null && b.getName().equals(name))
                .findFirst();
    }
}
