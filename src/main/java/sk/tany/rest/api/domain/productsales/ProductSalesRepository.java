package sk.tany.rest.api.domain.productsales;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface ProductSalesRepository extends MongoRepository<ProductSales, String> {
    public Optional<ProductSales> findByProductId(String productId) ;
}
