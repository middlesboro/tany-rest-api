package sk.tany.rest.api.service.admin.impl;

import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import sk.tany.rest.api.dto.features.InvoiceDataDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.domain.shopsettings.ShopSettings;
import sk.tany.rest.api.domain.shopsettings.ShopSettingsRepository;
import sk.tany.rest.api.dto.PriceBreakDown;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CarrierRepository carrierRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ShopSettingsRepository shopSettingsRepository;

    @Mock
    private sk.tany.rest.api.client.TanyFeaturesClient tanyFeaturesClient;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void generateInvoice_OrderPaid_ShouldContainPaidText() throws IOException {
        ShopSettings settings = new ShopSettings();
        settings.setShopEmail("info@tany.sk");
        settings.setShopPhoneNumber("421944432457");
        when(shopSettingsRepository.getFirstShopSettings()).thenReturn(settings);

        String orderId = "order-123";
        Order order = new Order();
        order.setId(orderId);
        order.setOrderIdentifier(12345L);
        order.setCreateDate(Instant.now());
        order.setStatus(OrderStatus.PAID);
        order.setPriceBreakDown(new PriceBreakDown());
        order.getPriceBreakDown().setTotalPrice(BigDecimal.TEN);
        order.getPriceBreakDown().setTotalPriceWithoutVat(BigDecimal.TEN);
        order.getPriceBreakDown().setTotalPriceVatValue(BigDecimal.ZERO);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        order.setCarrierId("carrier-1");
        order.setPaymentId("payment-1");
        when(carrierRepository.findById("carrier-1")).thenReturn(Optional.empty());
        when(paymentRepository.findById("payment-1")).thenReturn(Optional.empty());

        when(tanyFeaturesClient.generateInvoice(org.mockito.ArgumentMatchers.any())).thenReturn(new byte[]{1, 2, 3});

        byte[] pdfBytes = invoiceService.generateInvoice(orderId);

        Assertions.assertNotNull(pdfBytes);
        Assertions.assertTrue(pdfBytes.length > 0);

        org.mockito.Mockito.verify(tanyFeaturesClient).generateInvoice(org.mockito.ArgumentMatchers.argThat(dto -> "PAID".equals(dto.getStatus())));
    }

    @Test
    void generateInvoice_shouldUseCorrectDocumentNumberFormat() throws IOException {
        ShopSettings settings = new ShopSettings();
        settings.setShopEmail("info@tany.sk");
        settings.setShopPhoneNumber("421944432457");
        when(shopSettingsRepository.getFirstShopSettings()).thenReturn(settings);

        String orderId = "order-doc-1";
        Order order = new Order();
        order.setId(orderId);
        order.setOrderIdentifier(3L);
        // Create date in 2026
        order.setCreateDate(Instant.parse("2026-05-10T10:00:00Z"));
        order.setStatus(OrderStatus.CREATED);
        order.setPriceBreakDown(new PriceBreakDown());
        order.getPriceBreakDown().setTotalPrice(BigDecimal.TEN);
        order.getPriceBreakDown().setTotalPriceWithoutVat(BigDecimal.TEN);
        order.getPriceBreakDown().setTotalPriceVatValue(BigDecimal.ZERO);

        order.setCarrierId("carrier-1");
        order.setPaymentId("payment-1");
        when(carrierRepository.findById("carrier-1")).thenReturn(Optional.empty());
        when(paymentRepository.findById("payment-1")).thenReturn(Optional.empty());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(tanyFeaturesClient.generateInvoice(org.mockito.ArgumentMatchers.any())).thenReturn(new byte[]{1, 2, 3});

        byte[] pdfBytes = invoiceService.generateInvoice(orderId);

        org.mockito.Mockito.verify(tanyFeaturesClient).generateInvoice(org.mockito.ArgumentMatchers.argThat(dto -> dto.getOrderIdentifier() != null && dto.getOrderIdentifier().equals(3L)));
    }

    @Test
    void generateCreditNote_shouldDisplayNegativeDiscountInCreditNote() throws IOException {
        ShopSettings settings = new ShopSettings();
        settings.setShopEmail("info@tany.sk");
        settings.setShopPhoneNumber("421944432457");
        when(shopSettingsRepository.getFirstShopSettings()).thenReturn(settings);

        String orderId = "order-cn-1";
        Order order = new Order();
        order.setId(orderId);
        order.setOrderIdentifier(50L);
        order.setCreditNoteIdentifier(50L);
        order.setCancelDate(Instant.parse("2026-06-01T10:00:00Z"));
        order.setStatus(OrderStatus.CANCELED);
        order.setCarrierId("carrier-1");
        order.setPaymentId("payment-1");

        PriceBreakDown pbd = new PriceBreakDown();
        sk.tany.rest.api.dto.PriceItem discountItem = new sk.tany.rest.api.dto.PriceItem();
        discountItem.setType(sk.tany.rest.api.dto.PriceItemType.DISCOUNT);
        discountItem.setName("Special Discount");
        discountItem.setQuantity(1);
        // Assuming discount is stored as negative
        discountItem.setPriceWithVat(new BigDecimal("-10.00"));
        discountItem.setPriceWithoutVat(new BigDecimal("-8.33"));
        discountItem.setVatValue(new BigDecimal("-1.67"));

        pbd.setItems(java.util.List.of(discountItem));
        pbd.setTotalPrice(new BigDecimal("-10.00")); // Only discount for simplicity
        pbd.setTotalPriceWithoutVat(new BigDecimal("-8.33"));
        pbd.setTotalPriceVatValue(new BigDecimal("-1.67"));

        order.setPriceBreakDown(pbd);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(carrierRepository.findById("carrier-1")).thenReturn(Optional.empty());
        when(paymentRepository.findById("payment-1")).thenReturn(Optional.empty());

        when(tanyFeaturesClient.generateCreditNote(org.mockito.ArgumentMatchers.any())).thenReturn(new byte[]{1, 2, 3});

        byte[] pdfBytes = invoiceService.generateCreditNote(orderId);

        org.mockito.Mockito.verify(tanyFeaturesClient).generateCreditNote(org.mockito.ArgumentMatchers.argThat(dto -> dto.getOrderIdentifier() != null && dto.getOrderIdentifier().equals(50L)));
    }

    @Test
    void generateInvoice_WhenOrderIsCanceled_ShouldStillGenerateInvoice() throws IOException {
        ShopSettings settings = new ShopSettings();
        settings.setShopEmail("info@tany.sk");
        settings.setShopPhoneNumber("421944432457");
        when(shopSettingsRepository.getFirstShopSettings()).thenReturn(settings);

        String orderId = "order-canceled-invoice";
        Order order = new Order();
        order.setId(orderId);
        order.setOrderIdentifier(100L);
        order.setCreateDate(Instant.parse("2026-01-01T10:00:00Z"));
        order.setStatus(OrderStatus.CANCELED);
        order.setCancelDate(Instant.parse("2026-02-01T10:00:00Z"));
        order.setPriceBreakDown(new PriceBreakDown());
        order.getPriceBreakDown().setTotalPrice(BigDecimal.TEN);
        order.getPriceBreakDown().setTotalPriceWithoutVat(BigDecimal.TEN);
        order.getPriceBreakDown().setTotalPriceVatValue(BigDecimal.ZERO);

        order.setCarrierId("carrier-1");
        order.setPaymentId("payment-1");
        when(carrierRepository.findById("carrier-1")).thenReturn(Optional.empty());
        when(paymentRepository.findById("payment-1")).thenReturn(Optional.empty());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(tanyFeaturesClient.generateInvoice(org.mockito.ArgumentMatchers.any())).thenReturn(new byte[]{1, 2, 3});

        byte[] pdfBytes = invoiceService.generateInvoice(orderId);

        org.mockito.Mockito.verify(tanyFeaturesClient).generateInvoice(org.mockito.ArgumentMatchers.argThat(dto -> dto.getOrderIdentifier() != null && dto.getOrderIdentifier().equals(100L)));
    }

    @Test
    void generateInvoice_shouldContainFooter() throws IOException {
        ShopSettings settings = new ShopSettings();
        settings.setShopEmail("info@tany.sk");
        settings.setShopPhoneNumber("421944432457");
        when(shopSettingsRepository.getFirstShopSettings()).thenReturn(settings);

        String orderId = "order-footer-1";
        Order order = new Order();
        order.setId(orderId);
        order.setOrderIdentifier(999L);
        order.setCreateDate(Instant.now());
        order.setStatus(OrderStatus.CREATED);
        order.setPriceBreakDown(new PriceBreakDown());
        order.getPriceBreakDown().setTotalPrice(BigDecimal.ZERO);
        order.getPriceBreakDown().setTotalPriceWithoutVat(BigDecimal.ZERO);
        order.getPriceBreakDown().setTotalPriceVatValue(BigDecimal.ZERO);

        order.setCarrierId("carrier-1");
        order.setPaymentId("payment-1");
        when(carrierRepository.findById("carrier-1")).thenReturn(Optional.empty());
        when(paymentRepository.findById("payment-1")).thenReturn(Optional.empty());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(tanyFeaturesClient.generateInvoice(org.mockito.ArgumentMatchers.any())).thenReturn(new byte[]{1, 2, 3});

        byte[] pdfBytes = invoiceService.generateInvoice(orderId);

        org.mockito.Mockito.verify(tanyFeaturesClient).generateInvoice(org.mockito.ArgumentMatchers.argThat(dto -> "info@tany.sk".equals(dto.getShopEmail()) && "421944432457".equals(dto.getShopPhone())));
    }
}
