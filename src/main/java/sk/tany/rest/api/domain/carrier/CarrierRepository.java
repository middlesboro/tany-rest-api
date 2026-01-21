package sk.tany.rest.api.domain.carrier;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

@Repository
public class CarrierRepository extends AbstractInMemoryRepository<Carrier> {

    public CarrierRepository(Nitrite nitrite) {
        super(nitrite, Carrier.class);
    }
}
