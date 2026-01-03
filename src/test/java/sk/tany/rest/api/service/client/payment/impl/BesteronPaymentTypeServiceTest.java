package sk.tany.rest.api.service.client.payment.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.dto.besteron.BesteronIntentRequest;
import sk.tany.rest.api.dto.besteron.BesteronIntentResponse;
import sk.tany.rest.api.dto.besteron.BesteronTokenResponse;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BesteronPaymentTypeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BesteronPaymentTypeService service;

    @Test
    void getSupportedType_ReturnsBesteron() {
        assertEquals(PaymentType.BESTERON, service.getSupportedType());
    }

    @Test
    void getPaymentInfo_SuccessfulFlow() {
        // Arrange
        ReflectionTestUtils.setField(service, "clientId", "testClient");
        ReflectionTestUtils.setField(service, "clientSecret", "testSecret");
        ReflectionTestUtils.setField(service, "baseUrl", "http://test.com");
        ReflectionTestUtils.setField(service, "returnUrl", "http://return.com");
        ReflectionTestUtils.setField(service, "notificationUrl", "http://notification.com");

        OrderDto order = new OrderDto();
        order.setId("order1");
        order.setCustomerId("customer1");
        order.setFinalPrice(new BigDecimal("10.50"));
        order.setOrderIdentifier(12345L);

        Customer customer = new Customer();
        customer.setId("customer1");
        customer.setEmail("test@test.com");
        customer.setFirstname("John");
        customer.setLastname("Doe");

        when(customerRepository.findById("customer1")).thenReturn(Optional.of(customer));

        PaymentDto payment = new PaymentDto();

        BesteronTokenResponse tokenResponse = new BesteronTokenResponse("token123", 3600, "bearer");
        when(restTemplate.postForEntity(eq("http://test.com/api/oauth2/token"), any(HttpEntity.class), eq(BesteronTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(tokenResponse));

        BesteronIntentResponse intentResponse = new BesteronIntentResponse("http://payment.link", "trans123");
        when(restTemplate.postForEntity(eq("http://test.com/api/payment-intent"), any(HttpEntity.class), eq(BesteronIntentResponse.class)))
                .thenReturn(ResponseEntity.ok(intentResponse));

        // Act
        PaymentInfoDto result = service.getPaymentInfo(order, payment);

        // Assert
        assertNotNull(result);
        assertEquals("http://payment.link", result.getPaymentLink());
    }
}
