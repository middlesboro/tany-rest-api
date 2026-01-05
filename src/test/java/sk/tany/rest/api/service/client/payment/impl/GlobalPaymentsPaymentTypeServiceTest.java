package sk.tany.rest.api.service.client.payment.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.payment.GlobalPaymentsPaymentRepository;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalPaymentsPaymentTypeServiceTest {

    @Mock
    private GlobalPaymentsPaymentRepository globalPaymentsPaymentRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private GlobalPaymentsPaymentTypeService service;

    @Test
    public void testGetSupportedType() {
        assertEquals(PaymentType.GLOBAL_PAYMENTS, service.getSupportedType());
    }

    @Test
    public void testGetPaymentInfo() {
        // Set baseUrl via reflection or constructor if possible, but @Value is hard to set in unit test without spring context
        // We can just verify the link structure relative to what we expect, assuming default null for baseUrl or verify logic.
        // Actually, with @InjectMocks, @Value fields are null unless set.
        try {
            java.lang.reflect.Field baseUrlField = GlobalPaymentsPaymentTypeService.class.getDeclaredField("baseUrl");
            baseUrlField.setAccessible(true);
            baseUrlField.set(service, "http://localhost:8080");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        OrderDto order = new OrderDto();
        order.setId("order123");
        PaymentDto payment = new PaymentDto();

        PaymentInfoDto info = service.getPaymentInfo(order, payment);

        assertNotNull(info);
        assertEquals("http://localhost:8080/api/payments/global-payments/redirect/order123", info.getPaymentLink());
    }
}
