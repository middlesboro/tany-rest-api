package sk.tany.rest.api.domain.onedrive;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

@Repository
public class OneDriveTokenRepository extends AbstractInMemoryRepository<OneDriveToken> {

    public OneDriveTokenRepository(Nitrite nitrite) {
        super(nitrite, OneDriveToken.class);
    }
}
