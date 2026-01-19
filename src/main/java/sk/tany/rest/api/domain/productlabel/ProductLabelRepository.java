package sk.tany.rest.api.domain.productlabel;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductLabelRepository extends MongoRepository<ProductLabel, String> {
    List<ProductLabel> findAllByProductId(String productId);
}
