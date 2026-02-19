package sk.tany.rest.api.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.dto.PriceBreakDown;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.event.OrderStatusChangedEvent;
import sk.tany.rest.api.service.admin.InvoiceService;
import sk.tany.rest.api.service.common.EmailService;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerPriceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private sk.tany.rest.api.config.EshopConfig eshopConfig;
    @Mock
    private sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository shopSettingsRepository;
    @Mock
    private sk.tany.rest.api.service.client.ProductClientService productClientService;

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @BeforeEach
    void setUp() {
        lenient().when(eshopConfig.getFrontendUrl()).thenReturn("http://localhost:3000");
        Resource pdfResource = new ByteArrayResource("dummy pdf".getBytes());
        lenient().when(resourceLoader.getResource("classpath:formular-na-odstupenie-od-zmluvy-tany.sk.pdf")).thenReturn(pdfResource);
        lenient().when(resourceLoader.getResource("classpath:obchodne-podmienky.pdf")).thenReturn(pdfResource);
        lenient().when(invoiceService.generateInvoice(anyString())).thenReturn(new byte[0]);

        sk.tany.rest.api.domain.shopsettings.ShopSettings settings = new sk.tany.rest.api.domain.shopsettings.ShopSettings();
        settings.setShopEmail("test@test.com");
        settings.setShopPhoneNumber("123456789");
        lenient().when(shopSettingsRepository.getFirstShopSettings()).thenReturn(settings);
    }

    @Test
    void handleOrderStatusChanged_shouldFormatPriceWithNonBreakingSpace() {
        Order order = new Order();
        order.setId("order1");
        order.setOrderIdentifier(123L);
        order.setFirstname("John");
        order.setEmail("user@example.com");
        order.setStatus(OrderStatus.CREATED);
        order.setCarrierId("carrier1");
        order.setPaymentId("payment1");
        order.setStatusHistory(new ArrayList<>());
        order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.CREATED, Instant.now()));

        PriceBreakDown priceBreakDown = new PriceBreakDown();
        // Product
        PriceItem productItem = new PriceItem();
        productItem.setType(PriceItemType.PRODUCT);
        productItem.setName("Test Product");
        productItem.setQuantity(1);
        productItem.setPriceWithVat(new BigDecimal("9.50"));

        // Carrier
        PriceItem carrierItem = new PriceItem();
        carrierItem.setType(PriceItemType.CARRIER);
        carrierItem.setPriceWithVat(new BigDecimal("3.50"));

        // Payment
        PriceItem paymentItem = new PriceItem();
        paymentItem.setType(PriceItemType.PAYMENT);
        paymentItem.setPriceWithVat(new BigDecimal("1.00"));

        priceBreakDown.setItems(Arrays.asList(productItem, carrierItem, paymentItem));
        priceBreakDown.setTotalPrice(new BigDecimal("14.00"));
        order.setPriceBreakDown(priceBreakDown);

        when(carrierRepository.findById("carrier1")).thenReturn(Optional.of(new Carrier()));
        when(paymentRepository.findById("payment1")).thenReturn(Optional.of(new Payment()));

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(order);
        orderEventHandler.handleOrderStatusChanged(event);

        org.mockito.ArgumentCaptor<String> bodyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(eq("user@example.com"), anyString(), bodyCaptor.capture(), eq(true), any(File.class), any(File.class), any(File.class));

        String emailBody = bodyCaptor.getValue();

        // Check for non-breaking space
        // Product price
        assertTrue(emailBody.contains("9.50&nbsp;€") || emailBody.contains("9,50&nbsp;€"),
                "Product price should contain non-breaking space: " + emailBody);

        // Final price
        assertTrue(emailBody.contains("14.00&nbsp;€") || emailBody.contains("14,00&nbsp;€"),
                "Final price should contain non-breaking space: " + emailBody);
    }
}
