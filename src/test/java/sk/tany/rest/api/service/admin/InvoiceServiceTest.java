package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.carrier.Carrier;
import sk.tany.rest.api.domain.carrier.CarrierRepository;
import sk.tany.rest.api.domain.customer.Address;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderItem;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.Payment;
import sk.tany.rest.api.domain.payment.PaymentRepository;
import sk.tany.rest.api.domain.product.Product;
import sk.tany.rest.api.domain.product.ProductRepository;
import sk.tany.rest.api.dto.PriceBreakDown;
import sk.tany.rest.api.dto.PriceItem;
import sk.tany.rest.api.dto.PriceItemType;
import sk.tany.rest.api.service.admin.impl.InvoiceServiceImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

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
    public void generateInvoice_shouldReturnPdfBytes() {
        String orderId = "order123";
        Order order = new Order();
        order.setId(orderId);
        order.setCreateDate(Instant.now());
        order.setCarrierId("c1");
        order.setPaymentId("p1");
        order.setCustomerId("cust1");
        order.setDeliveryPrice(BigDecimal.TEN);

        OrderItem item = new OrderItem();
        item.setId("prod1");
        item.setName("Test Product");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("24.00")); // With VAT
        order.setItems(Collections.singletonList(item));

        PriceBreakDown pbd = new PriceBreakDown();
        PriceItem pi = new PriceItem(PriceItemType.PRODUCT, "prod1", "Test Product", 2, new BigDecimal("48.00"), new BigDecimal("40.00"), new BigDecimal("8.00"));
        pbd.setItems(List.of(pi));
        pbd.setTotalPrice(new BigDecimal("48.00"));
        pbd.setTotalPriceWithoutVat(new BigDecimal("40.00"));
        pbd.setTotalPriceVatValue(new BigDecimal("8.00"));
        order.setPriceBreakDown(pbd);

        Address address = new Address("Street 1", "City", "12345", "Slovakia");
        order.setInvoiceAddress(address);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Carrier carrier = new Carrier();
        carrier.setName("CarrierName");
        when(carrierRepository.findById("c1")).thenReturn(Optional.of(carrier));

        Payment payment = new Payment();
        payment.setName("PaymentName");
        when(paymentRepository.findById("p1")).thenReturn(Optional.of(payment));

        Customer customer = new Customer();
        customer.setFirstname("John");
        customer.setLastname("Doe");
        when(customerRepository.findById("cust1")).thenReturn(Optional.of(customer));

        Product product = new Product();
        product.setId("prod1");
        product.setProductCode("CODE123");
        product.setEan("EAN123");
        when(productRepository.findAllById(anyList())).thenReturn(List.of(product));

        byte[] result = invoiceService.generateInvoice(orderId);

        assertNotNull(result);
        assertTrue(result.length > 0, "PDF content should not be empty");

        // Basic check for PDF signature
        assertTrue(result[0] == '%' && result[1] == 'P' && result[2] == 'D' && result[3] == 'F', "Should start with %PDF");
    }

    @Test
    public void generateInvoice_shouldReturnPdfBytes_whenOrderCanceled() {
        String orderId = "orderCanceled";
        Order order = new Order();
        order.setId(orderId);
        order.setCreateDate(Instant.now());
        order.setCancelDate(Instant.now());
        order.setCreditNoteIdentifier(1001L);
        order.setStatus(sk.tany.rest.api.domain.order.OrderStatus.CANCELED);
        order.setCarrierId("c1");
        order.setPaymentId("p1");
        order.setCustomerId("cust1");
        order.setDeliveryPrice(BigDecimal.TEN);

        OrderItem item = new OrderItem();
        item.setId("prod1");
        item.setName("Test Product");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("24.00")); // With VAT
        order.setItems(Collections.singletonList(item));

        PriceBreakDown pbd = new PriceBreakDown();
        PriceItem pi = new PriceItem(PriceItemType.PRODUCT, "prod1", "Test Product", 2, new BigDecimal("48.00"), new BigDecimal("40.00"), new BigDecimal("8.00"));
        pbd.setItems(List.of(pi));
        pbd.setTotalPrice(new BigDecimal("48.00"));
        pbd.setTotalPriceWithoutVat(new BigDecimal("40.00"));
        pbd.setTotalPriceVatValue(new BigDecimal("8.00"));
        order.setPriceBreakDown(pbd);

        Address address = new Address("Street 1", "City", "12345", "Slovakia");
        order.setInvoiceAddress(address);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Carrier carrier = new Carrier();
        carrier.setName("CarrierName");
        when(carrierRepository.findById("c1")).thenReturn(Optional.of(carrier));

        Payment payment = new Payment();
        payment.setName("PaymentName");
        when(paymentRepository.findById("p1")).thenReturn(Optional.of(payment));

        Customer customer = new Customer();
        customer.setFirstname("John");
        customer.setLastname("Doe");
        when(customerRepository.findById("cust1")).thenReturn(Optional.of(customer));

        Product product = new Product();
        product.setId("prod1");
        product.setProductCode("CODE123");
        product.setEan("EAN123");
        when(productRepository.findAllById(anyList())).thenReturn(List.of(product));

        byte[] result = invoiceService.generateInvoice(orderId);

        assertNotNull(result);
        assertTrue(result.length > 0, "PDF content should not be empty");
        assertTrue(result[0] == '%' && result[1] == 'P' && result[2] == 'D' && result[3] == 'F', "Should start with %PDF");
    }
}
