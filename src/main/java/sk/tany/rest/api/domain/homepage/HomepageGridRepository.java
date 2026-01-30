package sk.tany.rest.api.domain.homepage;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

@Repository
public class HomepageGridRepository extends AbstractInMemoryRepository<HomepageGrid> {

    public HomepageGridRepository(Nitrite nitrite) {
        super(nitrite, HomepageGrid.class);
    }
}
