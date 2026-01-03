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
import sk.tany.rest.api.domain.order.Order;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.payment.BesteronPayment;
import sk.tany.rest.api.domain.payment.BesteronPaymentRepository;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.*;
import sk.tany.rest.api.dto.besteron.BesteronIntentRequest;
import sk.tany.rest.api.dto.besteron.BesteronIntentResponse;
import sk.tany.rest.api.dto.besteron.BesteronTokenResponse;
import sk.tany.rest.api.dto.besteron.BesteronTransactionResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BesteronPaymentTypeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BesteronPaymentRepository besteronPaymentRepository;

    @Mock
    private OrderRepository orderRepository;

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

        OrderItemDto item = new OrderItemDto();
        item.setName("Test Product");
        item.setPrice(new BigDecimal("10.50"));
        item.setQuantity(1);
        order.setItems(Collections.singletonList(item));

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

        verify(besteronPaymentRepository).save(any(BesteronPayment.class));
    }

    @Test
    void checkStatus_UpdatesOrderWhenCompleted() {
        // Arrange
        ReflectionTestUtils.setField(service, "clientId", "testClient");
        ReflectionTestUtils.setField(service, "clientSecret", "testSecret");
        ReflectionTestUtils.setField(service, "baseUrl", "http://test.com");

        String orderId = "order1";
        BesteronPayment besteronPayment = new BesteronPayment();
        besteronPayment.setTransactionId("trans123");
        besteronPayment.setStatus("Created");
        when(besteronPaymentRepository.findTopByOrderIdOrderByCreateDateDesc(orderId)).thenReturn(Optional.of(besteronPayment));

        BesteronTokenResponse tokenResponse = new BesteronTokenResponse("token123", 3600, "bearer");
        when(restTemplate.postForEntity(eq("http://test.com/api/oauth2/token"), any(HttpEntity.class), eq(BesteronTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(tokenResponse));

        BesteronTransactionResponse.Transaction transaction = new BesteronTransactionResponse.Transaction();
        transaction.setStatus("Completed");
        BesteronTransactionResponse statusResponse = new BesteronTransactionResponse();
        statusResponse.setTransaction(transaction);

        when(restTemplate.postForEntity(eq("http://test.com/api/payment-intents/trans123"), any(HttpEntity.class), eq(BesteronTransactionResponse.class)))
                .thenReturn(ResponseEntity.ok(statusResponse));

        Order order = new Order();
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        String status = service.checkStatus(orderId);

        // Assert
        assertEquals("Completed", status);
        verify(besteronPaymentRepository).save(besteronPayment);
        assertEquals("Completed", besteronPayment.getStatus());
        verify(orderRepository).save(order);
        assertEquals(OrderStatus.PAYED, order.getStatus());
    }

    @Test
    void checkStatus_NoUpdateWhenNotCompleted() {
        // Arrange
        ReflectionTestUtils.setField(service, "clientId", "testClient");
        ReflectionTestUtils.setField(service, "clientSecret", "testSecret");
        ReflectionTestUtils.setField(service, "baseUrl", "http://test.com");

        String orderId = "order1";
        BesteronPayment besteronPayment = new BesteronPayment();
        besteronPayment.setTransactionId("trans123");
        besteronPayment.setStatus("Created");
        when(besteronPaymentRepository.findTopByOrderIdOrderByCreateDateDesc(orderId)).thenReturn(Optional.of(besteronPayment));

        BesteronTokenResponse tokenResponse = new BesteronTokenResponse("token123", 3600, "bearer");
        when(restTemplate.postForEntity(eq("http://test.com/api/oauth2/token"), any(HttpEntity.class), eq(BesteronTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(tokenResponse));

        BesteronTransactionResponse.Transaction transaction = new BesteronTransactionResponse.Transaction();
        transaction.setStatus("WaitingForConfirmation");
        BesteronTransactionResponse statusResponse = new BesteronTransactionResponse();
        statusResponse.setTransaction(transaction);

        when(restTemplate.postForEntity(eq("http://test.com/api/payment-intents/trans123"), any(HttpEntity.class), eq(BesteronTransactionResponse.class)))
                .thenReturn(ResponseEntity.ok(statusResponse));

        // Act
        String status = service.checkStatus(orderId);

        // Assert
        assertEquals("WaitingForConfirmation", status);
        verify(besteronPaymentRepository).save(besteronPayment);
        assertEquals("WaitingForConfirmation", besteronPayment.getStatus());
        verify(orderRepository, never()).save(any());
    }
}
