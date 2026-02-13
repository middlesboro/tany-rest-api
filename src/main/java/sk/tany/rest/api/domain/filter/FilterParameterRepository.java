package sk.tany.rest.api.domain.filter;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilterParameterRepository extends MongoRepository<FilterParameter, String> {
    public Optional<FilterParameter> findByName(String name) ;

    public List<FilterParameter> findAllByFilterParameterValueIdsContaining(String valueId) ;
}
