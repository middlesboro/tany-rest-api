package sk.tany.rest.api.domain.filter;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface FilterParameterValueRepository extends MongoRepository<FilterParameterValue, String> {
    public Optional<FilterParameterValue> findByNameAndFilterParameterId(String name, String filterParameterId) ;
}
