package sk.tany.rest.api.domain.common;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface SequenceRepository extends MongoRepository<Sequence, String> {
    public Optional<Sequence> findById(String id) ;
}
