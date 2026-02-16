package sk.tany.rest.api.domain.auth;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface MagicLinkTokenRepository extends MongoRepository<MagicLinkToken, String> {
    public Optional<MagicLinkToken> findByJti(String jti) ;
}
