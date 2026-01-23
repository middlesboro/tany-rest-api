package sk.tany.rest.api.domain.jwk;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

@Repository
public class JwkKeyRepository extends AbstractInMemoryRepository<JwkKey> {
    public JwkKeyRepository(Nitrite nitrite) {
        super(nitrite, JwkKey.class);
    }
}
