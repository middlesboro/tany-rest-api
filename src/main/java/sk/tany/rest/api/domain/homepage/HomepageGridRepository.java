package sk.tany.rest.api.domain.homepage;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface HomepageGridRepository extends MongoRepository<HomepageGrid, String> {}
