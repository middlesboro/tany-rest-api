package sk.tany.rest.api.service.admin.impl;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Assertions;
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
import sk.tany.rest.api.dto.PriceBreakDown;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
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

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void generateInvoice_OrderPaid_ShouldContainPaidText() throws IOException {
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


        byte[] pdfBytes = invoiceService.generateInvoice(orderId);

        Assertions.assertNotNull(pdfBytes);
        Assertions.assertTrue(pdfBytes.length > 0);

        PdfReader reader = new PdfReader(pdfBytes);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        String text = extractor.getTextFromPage(1);

        Assertions.assertTrue(text.contains("Faktúra je už uhradená") || text.contains("FAKTÚRA JE UŽ UHRADEN"),
            "PDF should contain text about invoice being paid. Found content: " + text);
    }

    @Test
    void generateInvoice_shouldUseCorrectDocumentNumberFormat() throws IOException {
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

        byte[] pdfBytes = invoiceService.generateInvoice(orderId);
        PdfReader reader = new PdfReader(pdfBytes);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        String text = extractor.getTextFromPage(1);

        // Expected format: 2026000003
        Assertions.assertTrue(text.contains("2026000003"), "PDF should contain document number 2026000003. Found content: " + text);
    }

    @Test
    void generateInvoice_shouldDisplayNegativeDiscountInCreditNote() throws IOException {
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

        byte[] pdfBytes = invoiceService.generateInvoice(orderId);
        PdfReader reader = new PdfReader(pdfBytes);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        String text = extractor.getTextFromPage(1);

        // Document number check: 2026000050
        Assertions.assertTrue(text.contains("2026000050"), "PDF should contain credit note number 2026000050. Found: " + text);

        // Discount check: Should be "-10.00"
        // Since we force multiplier 1 for discount in credit note, -10 * 1 = -10.
        // It should appear as "-10.00 €"
        Assertions.assertTrue(text.contains("-10.00 €"), "PDF should contain negative discount value. Found: " + text);
    }
}
