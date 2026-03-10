package sk.tany.rest.api.domain.contentsnippet;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentSnippetRepository extends MongoRepository<ContentSnippet, String> {
}
