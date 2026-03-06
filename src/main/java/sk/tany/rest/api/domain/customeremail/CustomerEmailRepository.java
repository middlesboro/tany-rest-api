package sk.tany.rest.api.domain.customeremail;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerEmailRepository extends MongoRepository<CustomerEmail, String> {
}
