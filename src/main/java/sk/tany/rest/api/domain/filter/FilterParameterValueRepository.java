package sk.tany.rest.api.domain.filter;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterParameterValueRepository extends MongoRepository<FilterParameterValue, String> {
}
