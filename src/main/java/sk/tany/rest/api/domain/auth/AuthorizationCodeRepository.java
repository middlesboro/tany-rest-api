package sk.tany.rest.api.domain.auth;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorizationCodeRepository extends MongoRepository<AuthorizationCode, String> {
}
