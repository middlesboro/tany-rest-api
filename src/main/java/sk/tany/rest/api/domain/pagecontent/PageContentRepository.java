package sk.tany.rest.api.domain.pagecontent;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PageContentRepository extends MongoRepository<PageContent, String> {
    Optional<PageContent> findBySlug(String slug);
}
