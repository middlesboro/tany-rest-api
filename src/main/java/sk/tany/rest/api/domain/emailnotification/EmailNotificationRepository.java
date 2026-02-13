package sk.tany.rest.api.domain.emailnotification;

import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@Repository
public interface EmailNotificationRepository extends MongoRepository<EmailNotification, String> {
    public Optional<EmailNotification> findByEmailAndProductId(String email, String productId) ;
}
