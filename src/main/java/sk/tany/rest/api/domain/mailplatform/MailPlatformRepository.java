package sk.tany.rest.api.domain.mailplatform;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailPlatformRepository extends MongoRepository<MailPlatform, String> {
    List<MailPlatform> findByActiveTrue();
}
