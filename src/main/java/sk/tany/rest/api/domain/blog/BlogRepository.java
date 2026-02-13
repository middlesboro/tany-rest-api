package sk.tany.rest.api.domain.blog;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends MongoRepository<Blog, String> {
    Optional<Blog> findBySlug(String slug);

    List<Blog> findAllByVisibleTrueOrderByOrderAsc();
}
