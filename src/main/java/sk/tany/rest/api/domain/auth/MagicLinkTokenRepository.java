package sk.tany.rest.api.domain.auth;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MagicLinkTokenRepository extends MongoRepository<MagicLinkToken, String> {
    Optional<MagicLinkToken> findByJti(String jti);
}
