package sk.tany.features.domain.carrier;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface CarrierRepository extends MongoRepository<Carrier, String> {}
