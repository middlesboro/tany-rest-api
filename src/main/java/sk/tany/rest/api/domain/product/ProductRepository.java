package sk.tany.rest.api.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByCategoryIds(String categoryId, Pageable pageable);
}
