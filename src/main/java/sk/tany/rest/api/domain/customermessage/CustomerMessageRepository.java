package sk.tany.rest.api.domain.customermessage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerMessageRepository extends MongoRepository<CustomerMessage, String> {
}
