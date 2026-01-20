package sk.tany.rest.api.domain.productlabel;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductLabelRepository extends MongoRepository<ProductLabel, String> {
    java.util.Optional<ProductLabel> findByTitle(String title);
}
