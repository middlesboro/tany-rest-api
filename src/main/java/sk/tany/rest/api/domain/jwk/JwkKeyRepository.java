package sk.tany.rest.api.domain.jwk;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface JwkKeyRepository extends MongoRepository<JwkKey, String> {}
