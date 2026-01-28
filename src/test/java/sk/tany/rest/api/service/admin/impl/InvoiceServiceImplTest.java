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
        // Strict stubbing might complain if these are called with null, but getCarrierId() is null.
        // InvoiceServiceImpl handles nulls gracefully by checking findById(null) which usually returns empty or throws exception depending on repo impl.
        // But here we are mocking the interface.
        // Repository.findById(ID) where ID is null.
        // Mockito default answer is null or empty depending on return type.
        // findById returns Optional.
        // So passing null to findById might be problematic if not configured.
        // Let's set carrierId and paymentId to avoid nulls if possible, or just accept that Mockito handles it.
        // Actually, InvoiceServiceImpl does: carrierRepository.findById(order.getCarrierId())
        // If carrierId is null, it calls findById(null).

        // Let's make it robust.
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

        // Check if text contains the expected string.
        // Note: PdfTextExtractor with CP1250 fonts might have issues with some characters (like Á being `), so we check for the main part.
        Assertions.assertTrue(text.contains("Faktúra je už uhradená") || text.contains("FAKTÚRA JE UŽ UHRADEN"),
            "PDF should contain text about invoice being paid. Found content: " + text);
    }
}
