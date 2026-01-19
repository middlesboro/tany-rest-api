package sk.tany.rest.api.domain.productsales;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductSalesRepository extends MongoRepository<ProductSales, String> {
    Optional<ProductSales> findByProductId(String productId);
}
