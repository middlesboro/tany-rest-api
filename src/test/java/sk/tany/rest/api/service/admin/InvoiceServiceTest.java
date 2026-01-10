package sk.tany.rest.api.service.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.dto.*;
import sk.tany.rest.api.service.admin.*;
import sk.tany.rest.api.service.admin.impl.InvoiceServiceImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

    @Mock
    private OrderAdminService orderAdminService;
    @Mock
    private CarrierAdminService carrierAdminService;
    @Mock
    private PaymentAdminService paymentAdminService;
    @Mock
    private ProductAdminService productAdminService;
    @Mock
    private CustomerAdminService customerAdminService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    public void generateInvoice_shouldReturnPdfBytes() {
        String orderId = "order123";
        OrderDto order = new OrderDto();
        order.setId(orderId);
        order.setCreateDate(Instant.now());
        order.setCarrierId("c1");
        order.setPaymentId("p1");
        order.setCustomerId("cust1");
        order.setDeliveryPrice(BigDecimal.TEN);

        OrderItemDto item = new OrderItemDto();
        item.setId("prod1");
        item.setName("Test Product");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("24.00")); // With VAT
        order.setItems(Collections.singletonList(item));

        AddressDto address = new AddressDto();
        address.setStreet("Street 1");
        address.setCity("City");
        address.setZip("12345");
        order.setInvoiceAddress(address);

        when(orderAdminService.findById(orderId)).thenReturn(Optional.of(order));

        CarrierDto carrier = new CarrierDto();
        carrier.setName("CarrierName");
        when(carrierAdminService.findById("c1")).thenReturn(Optional.of(carrier));

        PaymentDto payment = new PaymentDto();
        payment.setName("PaymentName");
        when(paymentAdminService.findById("p1")).thenReturn(Optional.of(payment));

        CustomerDto customer = new CustomerDto();
        customer.setFirstname("John");
        customer.setLastname("Doe");
        when(customerAdminService.findById("cust1")).thenReturn(Optional.of(customer));

        ProductDto product = new ProductDto();
        product.setId("prod1");
        product.setProductCode("CODE123");
        product.setEan("EAN123");
        when(productAdminService.findAllByIds(anyList())).thenReturn(List.of(product));

        byte[] result = invoiceService.generateInvoice(orderId);

        assertNotNull(result);
        assertTrue(result.length > 0, "PDF content should not be empty");

        // Basic check for PDF signature
        assertTrue(result[0] == '%' && result[1] == 'P' && result[2] == 'D' && result[3] == 'F', "Should start with %PDF");
    }
}
