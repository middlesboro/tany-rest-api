package sk.tany.rest.api.domain.auth;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class MagicLinkTokenRepository extends AbstractInMemoryRepository<MagicLinkToken> {

    public MagicLinkTokenRepository(Nitrite nitrite) {
        super(nitrite, MagicLinkToken.class);
    }

    public Optional<MagicLinkToken> findByJti(String jti) {
        return memoryCache.values().stream()
                .filter(t -> t.getJti().equals(jti))
                .findFirst();
    }
}
