package sk.tany.rest.api.service.client.payment.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.dto.besteron.BesteronIntentResponse;
import sk.tany.rest.api.dto.besteron.BesteronTokenResponse;
import sk.tany.rest.api.dto.besteron.BesteronTransactionResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BesteronPaymentTypeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BesteronPaymentRepository besteronPaymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private sk.tany.rest.api.config.BesteronConfig besteronConfig;

    @InjectMocks
    private BesteronPaymentTypeService service;

    @Test
    void getSupportedType_ReturnsBesteron() {
        assertEquals(PaymentType.BESTERON, service.getSupportedType());
    }

    @Test
    void getPaymentInfo_SuccessfulFlow() {
        // Arrange
        when(besteronConfig.getClientId()).thenReturn("testClient");
        when(besteronConfig.getClientSecret()).thenReturn("testSecret");
        when(besteronConfig.getBaseUrl()).thenReturn("http://test.com");
        when(besteronConfig.getReturnUrl()).thenReturn("http://return.com");
        when(besteronConfig.getNotificationUrl()).thenReturn("http://notification.com");

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
        lenient().when(restTemplate.postForEntity(eq("http://test.com/api/oauth2/token"), any(HttpEntity.class), eq(BesteronTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(tokenResponse));

        BesteronIntentResponse intentResponse = new BesteronIntentResponse("http://payment.link", "trans123");
        lenient().when(restTemplate.postForEntity(eq("http://test.com/api/payment-intent"), any(HttpEntity.class), eq(BesteronIntentResponse.class)))
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
        when(besteronConfig.getClientId()).thenReturn("testClient");
        when(besteronConfig.getApiKey()).thenReturn("testSecret");
        when(besteronConfig.getVerifyUrl()).thenReturn("http://test.com");

        String orderId = "order1";
        BesteronPayment besteronPayment = new BesteronPayment();
        besteronPayment.setTransactionId("trans123");
        besteronPayment.setStatus("Created");
        when(besteronPaymentRepository.findTopByOrderIdOrderByCreateDateDesc(orderId)).thenReturn(Optional.of(besteronPayment));

        BesteronTokenResponse tokenResponse = new BesteronTokenResponse("token123", 3600, "bearer");
        lenient().when(restTemplate.postForEntity(eq("http://test.com/api/oauth2/token"), any(HttpEntity.class), eq(BesteronTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(tokenResponse));

        BesteronTransactionResponse.Transaction transaction = new BesteronTransactionResponse.Transaction();
        transaction.setStatus("Completed");
        BesteronTransactionResponse statusResponse = new BesteronTransactionResponse();
        statusResponse.setTransaction(transaction);

        lenient().when(restTemplate.postForEntity(eq("http://test.com/api/payment-intents/trans123"), any(HttpEntity.class), eq(BesteronTransactionResponse.class)))
                .thenReturn(ResponseEntity.ok(statusResponse));

        Order order = new Order();
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        String status = service.checkStatus(orderId);

        // Assert
        assertEquals("COMPLETED", status);
        verify(besteronPaymentRepository).save(besteronPayment);
        assertEquals("COMPLETED", besteronPayment.getStatus());
        assertEquals("Completed", besteronPayment.getOriginalStatus());
        verify(orderRepository).save(order);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void checkStatus_NoUpdateWhenNotCompleted() {
        // Arrange
        when(besteronConfig.getClientId()).thenReturn("testClient");
        when(besteronConfig.getApiKey()).thenReturn("testSecret");
        when(besteronConfig.getVerifyUrl()).thenReturn("http://test.com");

        String orderId = "order1";
        BesteronPayment besteronPayment = new BesteronPayment();
        besteronPayment.setTransactionId("trans123");
        besteronPayment.setStatus("Created");
        when(besteronPaymentRepository.findTopByOrderIdOrderByCreateDateDesc(orderId)).thenReturn(Optional.of(besteronPayment));

        BesteronTokenResponse tokenResponse = new BesteronTokenResponse("token123", 3600, "bearer");
        lenient().when(restTemplate.postForEntity(eq("http://test.com/api/oauth2/token"), any(HttpEntity.class), eq(BesteronTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(tokenResponse));

        BesteronTransactionResponse.Transaction transaction = new BesteronTransactionResponse.Transaction();
        transaction.setStatus("WaitingForConfirmation");
        BesteronTransactionResponse statusResponse = new BesteronTransactionResponse();
        statusResponse.setTransaction(transaction);

        lenient().when(restTemplate.postForEntity(eq("http://test.com/api/payment-intents/trans123"), any(HttpEntity.class), eq(BesteronTransactionResponse.class)))
                .thenReturn(ResponseEntity.ok(statusResponse));

        // Act
        String status = service.checkStatus(orderId);

        // Assert
        assertEquals("WAITING", status);
        verify(besteronPaymentRepository).save(besteronPayment);
        assertEquals("WAITING", besteronPayment.getStatus());
        assertEquals("WaitingForConfirmation", besteronPayment.getOriginalStatus());
        verify(orderRepository, never()).save(any());
    }
}
