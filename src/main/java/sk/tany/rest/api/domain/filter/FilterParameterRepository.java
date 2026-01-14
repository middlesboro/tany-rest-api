package sk.tany.rest.api.domain.filter;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilterParameterRepository extends MongoRepository<FilterParameter, String> {
    List<FilterParameter> findAllByFilterParameterValueIdsContaining(String valueId);
}
