package sk.tany.rest.api.domain.productlabel;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class ProductLabelRepository extends AbstractInMemoryRepository<ProductLabel> {

    public ProductLabelRepository(Nitrite nitrite) {
        super(nitrite, ProductLabel.class);
    }

    public Optional<ProductLabel> findByTitle(String title) {
        return memoryCache.values().stream()
                .filter(pl -> pl.getTitle() != null && pl.getTitle().equals(title))
                .findFirst();
    }
}
