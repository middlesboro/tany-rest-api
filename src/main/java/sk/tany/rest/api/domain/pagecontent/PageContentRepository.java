package sk.tany.rest.api.domain.pagecontent;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface PageContentRepository extends MongoRepository<PageContent, String> {
    public Optional<PageContent> findBySlug(String slug) ;
}
