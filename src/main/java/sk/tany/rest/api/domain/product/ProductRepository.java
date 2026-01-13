package sk.tany.rest.api.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    Page<Product> findByCategoryIds(String categoryId, Pageable pageable);
    Optional<Product> findByPrestashopId(Long prestashopId);
}
