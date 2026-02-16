package sk.tany.rest.api.domain.productlabel;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface ProductLabelRepository extends MongoRepository<ProductLabel, String> {
    public Optional<ProductLabel> findByTitle(String title) ;
}
