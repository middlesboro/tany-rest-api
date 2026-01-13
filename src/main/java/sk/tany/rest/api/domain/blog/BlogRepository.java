package sk.tany.rest.api.domain.blog;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends MongoRepository<Blog, String> {
    java.util.List<Blog> findAllByVisibleTrue();
}
