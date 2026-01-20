package sk.tany.rest.api.domain.supplier;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class SupplierRepository extends AbstractInMemoryRepository<Supplier> {

    public SupplierRepository(Nitrite nitrite) {
        super(nitrite, Supplier.class);
    }

    public Optional<Supplier> findByPrestashopId(Long prestashopId) {
        return memoryCache.values().stream()
                .filter(s -> s.getPrestashopId() != null && s.getPrestashopId().equals(prestashopId))
                .findFirst();
    }

    public Optional<Supplier> findByName(String name) {
        return memoryCache.values().stream()
                .filter(s -> s.getName() != null && s.getName().equals(name))
                .findFirst();
    }
}
