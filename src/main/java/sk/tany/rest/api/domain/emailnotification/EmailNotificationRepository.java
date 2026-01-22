package sk.tany.rest.api.domain.emailnotification;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.stereotype.Repository;
import sk.tany.rest.api.domain.AbstractInMemoryRepository;

import java.util.Optional;

@Repository
public class EmailNotificationRepository extends AbstractInMemoryRepository<EmailNotification> {

    public EmailNotificationRepository(Nitrite nitrite) {
        super(nitrite, EmailNotification.class);
    }

    public Optional<EmailNotification> findByEmailAndProductId(String email, String productId) {
        return memoryCache.values().stream()
                .filter(n -> n.getEmail().equalsIgnoreCase(email) && n.getProductId().equals(productId))
                .findFirst();
    }
}
