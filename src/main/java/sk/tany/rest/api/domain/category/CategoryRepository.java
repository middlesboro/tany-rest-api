package sk.tany.rest.api.domain.category;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findByPrestashopId(Long prestashopId);
    List<Category> findAllByPrestashopIdIn(List<Long> prestashopIds);
}
