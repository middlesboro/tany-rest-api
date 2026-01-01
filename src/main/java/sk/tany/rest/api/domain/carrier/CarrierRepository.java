package sk.tany.rest.api.domain.carrier;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierRepository extends MongoRepository<Carrier, String> {
}
