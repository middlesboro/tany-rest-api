package sk.tany.rest.api.domain.productsales;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class ProductSalesRepository extends AbstractInMemoryRepository<ProductSales> {

    public ProductSalesRepository(Nitrite nitrite) {
        super(nitrite, ProductSales.class);
    }

    public Optional<ProductSales> findByProductId(String productId) {
        return memoryCache.values().stream()
                .filter(ps -> ps.getProductId() != null && ps.getProductId().equals(productId))
                .findFirst();
    }
}
