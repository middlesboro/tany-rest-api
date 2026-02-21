package sk.tany.rest.api.service.client.payment.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class BesteronPaymentTypeServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BesteronPaymentRepository besteronPaymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private BesteronPaymentTypeService service;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        service = new BesteronPaymentTypeService(builder.build(), besteronPaymentRepository, orderRepository, eventPublisher);
    }

    @Test
    void getSupportedType_ReturnsBesteron() {
        assertEquals(PaymentType.BESTERON, service.getSupportedType());
    }

    @Test
    void getPaymentInfo_SuccessfulFlow() throws Exception {
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

        PaymentDto payment = new PaymentDto();

        BesteronTokenResponse tokenResponse = new BesteronTokenResponse("token123", 3600, "bearer");
        mockServer.expect(requestTo("http://test.com/api/oauth2/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));

        BesteronIntentResponse intentResponse = new BesteronIntentResponse("http://payment.link", "trans123");
        mockServer.expect(requestTo("http://test.com/api/payment-intent"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(intentResponse), MediaType.APPLICATION_JSON));

        // Act
        PaymentInfoDto result = service.getPaymentInfo(order, payment);

        // Assert
        assertNotNull(result);
        assertEquals("http://payment.link", result.getPaymentLink());

        verify(besteronPaymentRepository).save(any(BesteronPayment.class));
        mockServer.verify();
    }

    @Test
    void checkStatus_UpdatesOrderWhenCompleted() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(service, "clientId", "testClient");
        ReflectionTestUtils.setField(service, "clientSecret", "testSecret");
        ReflectionTestUtils.setField(service, "baseUrl", "http://test.com");
        ReflectionTestUtils.setField(service, "verifyUrl", "http://verify.com"); // Need to set verifyUrl as it's used in checkStatus

        String orderId = "order1";
        BesteronPayment besteronPayment = new BesteronPayment();
        besteronPayment.setTransactionId("trans123");
        besteronPayment.setStatus("Created");
        when(besteronPaymentRepository.findTopByOrderIdOrderByCreateDateDesc(orderId)).thenReturn(Optional.of(besteronPayment));

        BesteronTokenResponse tokenResponse = new BesteronTokenResponse("token123", 3600, "bearer");
        // checkStatus calls getAuthToken(VERIFY) which uses verifyUrl if type is VERIFY?
        // Let's check implementation.
        // if (type == BesteronUrlType.BASE) ... else { besteronBaseUrl = verifyUrl; secret = apiKey; }
        // So expected URL is verifyUrl + /api/oauth2/token
        // I need to set apiKey too.
        ReflectionTestUtils.setField(service, "apiKey", "testApiKey");

        mockServer.expect(requestTo("http://verify.com/api/oauth2/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));

        BesteronTransactionResponse.Transaction transaction = new BesteronTransactionResponse.Transaction();
        transaction.setStatus("Completed");
        BesteronTransactionResponse statusResponse = new BesteronTransactionResponse();
        statusResponse.setTransaction(transaction);

        mockServer.expect(requestTo("http://verify.com/api/payment-intents/trans123"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(statusResponse), MediaType.APPLICATION_JSON));

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
        mockServer.verify();
    }

    @Test
    void checkStatus_NoUpdateWhenNotCompleted() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(service, "clientId", "testClient");
        ReflectionTestUtils.setField(service, "clientSecret", "testSecret");
        ReflectionTestUtils.setField(service, "baseUrl", "http://test.com");
        ReflectionTestUtils.setField(service, "verifyUrl", "http://verify.com");
        ReflectionTestUtils.setField(service, "apiKey", "testApiKey");


        String orderId = "order1";
        BesteronPayment besteronPayment = new BesteronPayment();
        besteronPayment.setTransactionId("trans123");
        besteronPayment.setStatus("Created");
        when(besteronPaymentRepository.findTopByOrderIdOrderByCreateDateDesc(orderId)).thenReturn(Optional.of(besteronPayment));

        BesteronTokenResponse tokenResponse = new BesteronTokenResponse("token123", 3600, "bearer");
        mockServer.expect(requestTo("http://verify.com/api/oauth2/token"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(tokenResponse), MediaType.APPLICATION_JSON));

        BesteronTransactionResponse.Transaction transaction = new BesteronTransactionResponse.Transaction();
        transaction.setStatus("WaitingForConfirmation");
        BesteronTransactionResponse statusResponse = new BesteronTransactionResponse();
        statusResponse.setTransaction(transaction);

        mockServer.expect(requestTo("http://verify.com/api/payment-intents/trans123"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(statusResponse), MediaType.APPLICATION_JSON));

        // Act
        String status = service.checkStatus(orderId);

        // Assert
        assertEquals("WAITING", status);
        verify(besteronPaymentRepository).save(besteronPayment);
        assertEquals("WAITING", besteronPayment.getStatus());
        assertEquals("WaitingForConfirmation", besteronPayment.getOriginalStatus());
        verify(orderRepository, never()).save(any());
        mockServer.verify();
    }
}
