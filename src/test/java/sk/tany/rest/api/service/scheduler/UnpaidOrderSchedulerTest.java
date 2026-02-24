package sk.tany.rest.api.service.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.config.EshopConfig;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.service.common.EmailService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnpaidOrderSchedulerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ShopSettingsRepository shopSettingsRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private EshopConfig eshopConfig;

    @InjectMocks
    private UnpaidOrderScheduler scheduler;

    private Order order;
    private Payment payment;
    private ShopSettings shopSettings;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId("order1");
        order.setOrderIdentifier(123L);
        order.setPaymentId("pay1");
        order.setEmail("test@example.com");
        order.setFirstname("John");
        order.setFinalPrice(BigDecimal.TEN);

        payment = new Payment();
        payment.setId("pay1");
        payment.setType(PaymentType.BANK_TRANSFER);

        shopSettings = new ShopSettings();
        shopSettings.setBankAccount("IBAN123");
        shopSettings.setShopEmail("support@tany.sk");

        lenient().when(paymentRepository.findById("pay1")).thenReturn(Optional.of(payment));
        lenient().when(shopSettingsRepository.getFirstShopSettings()).thenReturn(shopSettings);
        lenient().when(eshopConfig.getFrontendUrl()).thenReturn("http://localhost");
    }

    @Test
    void checkUnpaidOrders_BankTransfer_ShouldSendEmail() {
        // Arrange
        order.setCreateDate(Instant.now().minus(49, ChronoUnit.HOURS)); // > 48h
        payment.setType(PaymentType.BANK_TRANSFER);

        when(orderRepository.findAllByStatusAndCreateDateAfterAndPaymentNotificationDateIsNull(eq(OrderStatus.CREATED), any(Instant.class)))
                .thenReturn(List.of(order));

        // Act
        scheduler.checkUnpaidOrders();

        // Assert
        verify(emailService).sendEmail(eq("test@example.com"), contains("Nezaplatená objednávka"), anyString(), eq(true), isNull());
        verify(orderRepository).save(order);
    }

    @Test
    void checkUnpaidOrders_BankTransfer_TooSoon_ShouldNotSendEmail() {
        // Arrange
        order.setCreateDate(Instant.now().minus(47, ChronoUnit.HOURS)); // < 48h
        payment.setType(PaymentType.BANK_TRANSFER);

        when(orderRepository.findAllByStatusAndCreateDateAfterAndPaymentNotificationDateIsNull(eq(OrderStatus.CREATED), any(Instant.class)))
                .thenReturn(List.of(order));

        // Act
        scheduler.checkUnpaidOrders();

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), any());
        verify(orderRepository, never()).save(order);
    }

    @Test
    void checkUnpaidOrders_Online_ShouldSendEmail() {
        // Arrange
        order.setCreateDate(Instant.now().minus(31, ChronoUnit.MINUTES)); // > 30m
        payment.setType(PaymentType.BESTERON);

        when(orderRepository.findAllByStatusAndCreateDateAfterAndPaymentNotificationDateIsNull(eq(OrderStatus.CREATED), any(Instant.class)))
                .thenReturn(List.of(order));

        // Act
        scheduler.checkUnpaidOrders();

        // Assert
        verify(emailService).sendEmail(eq("test@example.com"), contains("Nezaplatená objednávka"), anyString(), eq(true), isNull());
        verify(orderRepository).save(order);
    }

    @Test
    void checkUnpaidOrders_Online_TooSoon_ShouldNotSendEmail() {
        // Arrange
        order.setCreateDate(Instant.now().minus(29, ChronoUnit.MINUTES)); // < 30m
        payment.setType(PaymentType.GLOBAL_PAYMENTS);

        when(orderRepository.findAllByStatusAndCreateDateAfterAndPaymentNotificationDateIsNull(eq(OrderStatus.CREATED), any(Instant.class)))
                .thenReturn(List.of(order));

        // Act
        scheduler.checkUnpaidOrders();

        // Assert
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), any());
        verify(orderRepository, never()).save(order);
    }
}
