package sk.tany.rest.api.domain.auth;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class AuthorizationCodeRepository extends AbstractInMemoryRepository<AuthorizationCode> {

    public AuthorizationCodeRepository(Nitrite nitrite) {
        super(nitrite, AuthorizationCode.class);
    }
}
