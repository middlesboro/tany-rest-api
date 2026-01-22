package sk.tany.rest.api.service.client.emailnotification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.emailnotification.EmailNotification;
import sk.tany.rest.api.domain.emailnotification.EmailNotificationRepository;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.client.emailnotification.EmailNotificationCreateRequest;
import sk.tany.rest.api.exception.EmailNotificationException;
import sk.tany.rest.api.exception.ProductException;

@Service
@RequiredArgsConstructor
public class EmailNotificationClientServiceImpl implements EmailNotificationClientService {

    private final EmailNotificationRepository emailNotificationRepository;
    private final ProductRepository productRepository;

    @Override
    public void createNotification(EmailNotificationCreateRequest request) {
        if (!productRepository.existsById(request.getProductId())) {
            throw new ProductException.NotFound("Product not found");
        }

        if (emailNotificationRepository.findByEmailAndProductId(request.getEmail(), request.getProductId()).isPresent()) {
            throw new EmailNotificationException.Conflict("Notification already exists");
        }

        EmailNotification notification = new EmailNotification();
        notification.setEmail(request.getEmail());
        notification.setProductId(request.getProductId());

        emailNotificationRepository.save(notification);
    }
}
