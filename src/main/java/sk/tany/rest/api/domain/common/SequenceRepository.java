package sk.tany.rest.api.domain.common;

import org.dizitart.no2.Nitrite;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class SequenceRepository extends AbstractInMemoryRepository<Sequence> {

    public SequenceRepository(Nitrite nitrite) {
        super(nitrite, Sequence.class);
    }

    public Optional<Sequence> findById(String id) {
        return Optional.ofNullable(memoryCache.get(id));
    }
}
