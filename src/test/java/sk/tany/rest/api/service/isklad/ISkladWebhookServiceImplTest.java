package sk.tany.rest.api.service.isklad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.isklad.ISkladOrderStatusUpdateRequest;
import sk.tany.rest.api.dto.isklad.ISkladPackage;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.service.common.SequenceService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ISkladWebhookServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SequenceService sequenceService;

    @Mock
    private EmailService emailService;
    @Mock
    private ShopSettingsRepository shopSettingsRepository;

    @InjectMocks
    private ISkladWebhookServiceImpl webhookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ShopSettings settings = new ShopSettings();
        settings.setShopEmail("test@test.com");
        settings.setShopPhoneNumber("123456789");
        lenient().when(shopSettingsRepository.getFirstShopSettings()).thenReturn(settings);
    }

    @Test
    void updateOrderStatus_shouldUpdateStatusAndTracking() {
        ISkladOrderStatusUpdateRequest request = new ISkladOrderStatusUpdateRequest();
        request.setOrderOriginalId("12345");
        request.setStatusId(5); // SENT
        ISkladPackage pkg = new ISkladPackage();
        pkg.setTrackingUrl("http://track.me/123");
        request.setPackages(Collections.singletonList(pkg));

        Order order = new Order();
        order.setId("nid1");
        order.setOrderIdentifier(12345L);
        order.setStatus(OrderStatus.CREATED);
        order.setEmail("user@example.com");
        order.setFirstname("John");

        when(orderRepository.findByOrderIdentifier(12345L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        webhookService.updateOrderStatus(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(OrderStatus.SENT, savedOrder.getStatus());
        assertEquals("http://track.me/123", savedOrder.getCarrierOrderStateLink());
        verify(emailService).sendEmail(eq("user@example.com"), eq("Objednávka odoslaná"), anyString(), eq(true), any());
    }

    @Test
    void updateOrderStatus_shouldHandleCanceled() {
        ISkladOrderStatusUpdateRequest request = new ISkladOrderStatusUpdateRequest();
        request.setOrderOriginalId("12345");
        request.setStatusId(15); // CANCELED

        Order order = new Order();
        order.setId("nid1");
        order.setOrderIdentifier(12345L);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findByOrderIdentifier(12345L)).thenReturn(Optional.of(order));
        when(sequenceService.getNextSequence("credit_note_identifier")).thenReturn(999L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        webhookService.updateOrderStatus(request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(OrderStatus.CANCELED, savedOrder.getStatus());
        assertNotNull(savedOrder.getCancelDate());
        assertEquals(999L, savedOrder.getCreditNoteIdentifier());
    }
}
