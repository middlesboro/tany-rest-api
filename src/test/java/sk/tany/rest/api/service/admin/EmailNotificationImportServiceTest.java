package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.emailnotification.EmailNotification;
import sk.tany.rest.api.domain.emailnotification.EmailNotificationRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationImportServiceTest {

    @Mock
    private EmailNotificationRepository emailNotificationRepository;

    @Mock
    private ProductRepository productRepository;

    @Test
    void shouldImportEmailNotifications() {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        EmailNotificationImportService service = new EmailNotificationImportService(emailNotificationRepository, productRepository, objectMapper);

        Product p1 = new Product();
        p1.setId("p1");
        p1.setProductIdentifier(443L);
        when(productRepository.findByProductIdentifier(443L)).thenReturn(Optional.of(p1));

        Product p2 = new Product();
        p2.setId("p2");
        p2.setProductIdentifier(2429L);
        when(productRepository.findByProductIdentifier(2429L)).thenReturn(Optional.of(p2));

        when(emailNotificationRepository.findByEmailAndProductId("mail0@gmail.com", "p1")).thenReturn(Optional.empty());
        when(emailNotificationRepository.findByEmailAndProductId("mail1@gmail.com", "p2")).thenReturn(Optional.empty());

        // Act
        service.importEmailNotifications();

        // Assert
        verify(emailNotificationRepository, times(2)).save(any(EmailNotification.class));
    }
}
