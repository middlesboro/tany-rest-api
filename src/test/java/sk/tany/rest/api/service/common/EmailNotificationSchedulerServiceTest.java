package sk.tany.rest.api.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sk.tany.rest.api.domain.emailnotification.EmailNotification;
import sk.tany.rest.api.domain.emailnotification.EmailNotificationRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationSchedulerServiceTest {

    @Mock
    private EmailNotificationRepository emailNotificationRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private ShopSettingsRepository shopSettingsRepository;

    @InjectMocks
    private EmailNotificationSchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(schedulerService, "frontendUrl", "http://localhost:3000");
        ShopSettings settings = new ShopSettings();
        settings.setShopEmail("test@test.com");
        settings.setShopPhoneNumber("123456789");
        lenient().when(shopSettingsRepository.getFirstShopSettings()).thenReturn(settings);
    }

    @Test
    void processBackInStockNotifications_shouldSendEmailWithLink() {
        // Arrange
        EmailNotification notification = new EmailNotification();
        notification.setId("notif1");
        notification.setEmail("test@example.com");
        notification.setProductId("prod1");

        Product product = new Product();
        product.setId("prod1");
        product.setTitle("Test Product");
        product.setSlug("test-product");
        product.setActive(true);
        product.setQuantity(10);
        product.setPrice(new BigDecimal("100.00"));
        product.setDiscountPrice(new BigDecimal("90.00"));

        when(emailNotificationRepository.findAll()).thenReturn(List.of(notification));
        when(productRepository.findAllById(any())).thenReturn(List.of(product));

        // Act
        schedulerService.processBackInStockNotifications();

        // Assert
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(eq("test@example.com"), anyString(), bodyCaptor.capture(), eq(true), any());

        String body = bodyCaptor.getValue();
        // Check for the link
        String expectedLink = "<a href='http://localhost:3000/produkt/test-product'>Test Product</a>";
        assertTrue(body.contains(expectedLink),
                "Email body should contain clickable product link.\nExpected: " + expectedLink + "\nActual Body: " + body);
    }
}
