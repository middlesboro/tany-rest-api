package sk.tany.rest.api.service.client.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.order.OrderStatus;
import sk.tany.rest.api.domain.order.OrderStatusHistory;
import sk.tany.rest.api.domain.payment.BesteronPayment;
import sk.tany.rest.api.domain.payment.BesteronPaymentRepository;
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.dto.besteron.BesteronIntentRequest;
import sk.tany.rest.api.dto.besteron.BesteronIntentResponse;
import sk.tany.rest.api.domain.payment.enums.PaymentStatus;
import sk.tany.rest.api.dto.besteron.BesteronTokenResponse;
import sk.tany.rest.api.dto.besteron.BesteronTransactionResponse;
import sk.tany.rest.api.service.client.payment.PaymentTypeService;
import sk.tany.rest.api.exception.PaymentException;
import sk.tany.rest.api.exception.CustomerException;
import sk.tany.rest.api.service.common.EmailService;
import sk.tany.rest.api.domain.order.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// TODO add everywhere log.info about created payment link etc. add also to other services not only payments
@Service
@Slf4j
@RequiredArgsConstructor
public class BesteronPaymentTypeService implements PaymentTypeService {

    private final RestTemplate restTemplate;
    private final CustomerRepository customerRepository;
    private final BesteronPaymentRepository besteronPaymentRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    private String emailPaidTemplate;

    // TODO add to config class
    @Value("${besteron.client-id}")
    private String clientId;

    @Value("${besteron.client-secret}")
    private String clientSecret;

    @Value("${besteron.base-url}")
    private String baseUrl;

    @Value("${besteron.return-url}")
    private String returnUrl;

    @Value("${besteron.notification-url}")
    private String notificationUrl;

    @Override
    public PaymentType getSupportedType() {
        return PaymentType.BESTERON;
    }

    @Override
    public PaymentInfoDto getPaymentInfo(OrderDto order, PaymentDto payment) {
        try {
            String token = getAuthToken();
            String paymentLink = createPaymentIntent(order, token);
            return PaymentInfoDto.builder()
                    .paymentLink(paymentLink)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create Besteron payment link for order {}", order.getId(), e);
            throw new PaymentException("Failed to generate Besteron payment link", e);
        }
    }

    public String checkStatus(String orderId) {
        Optional<BesteronPayment> paymentOpt = besteronPaymentRepository.findTopByOrderIdOrderByCreateDateDesc(orderId);
        if (paymentOpt.isEmpty()) {
            return null;
        }
        BesteronPayment payment = paymentOpt.get();
        try {
            String token = getAuthToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<BesteronTransactionResponse> response = restTemplate.postForEntity(
                    baseUrl + "/api/payment-intents/" + payment.getTransactionId(),
                    request,
                    BesteronTransactionResponse.class
            );

            if (response.getBody() != null && response.getBody().getTransaction() != null) {
                String status = response.getBody().getTransaction().getStatus();
                payment.setOriginalStatus(status);
                PaymentStatus paymentStatus = mapStatus(status);
                payment.setStatus(paymentStatus.name());
                besteronPaymentRepository.save(payment);

                if ("Completed".equalsIgnoreCase(status)) {
                    orderRepository.findById(orderId).ifPresent(order -> {
                        if (order.getStatus() != OrderStatus.PAID) {
                            order.setStatus(OrderStatus.PAID);
                            if (order.getStatusHistory() == null) {
                                order.setStatusHistory(new ArrayList<>());
                            }
                            order.getStatusHistory().add(new OrderStatusHistory(OrderStatus.PAID, Instant.now()));
                            Order savedOrder = orderRepository.save(order);
                            sendOrderPaidEmail(savedOrder);
                        }
                    });
                }
                return paymentStatus.name();
            }
        } catch (Exception e) {
            log.error("Failed to check status for order {}", orderId, e);
        }
        return payment.getStatus();
    }

    private PaymentStatus mapStatus(String status) {
        if (status == null) {
            return PaymentStatus.ERROR;
        }
        return switch (status) {
            case "Created" -> PaymentStatus.CREATED;
            case "WaitingForConfirmation" -> PaymentStatus.WAITING;
            case "Completed" -> PaymentStatus.COMPLETED;
            case "Canceled" -> PaymentStatus.CANCELED;
            case "Error", "Timeouted", "Invalid", "ManualAttentionRequired" -> PaymentStatus.ERROR;
            default -> PaymentStatus.ERROR;
        };
    }

    private String getAuthToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<BesteronTokenResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/oauth2/token",
                request,
                BesteronTokenResponse.class
        );

        if (response.getBody() == null || response.getBody().getAccessToken() == null) {
            throw new PaymentException("Failed to retrieve access token from Besteron");
        }

        return response.getBody().getAccessToken();
    }

    private String createPaymentIntent(OrderDto order, String token) {
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new CustomerException.NotFound("Customer not found for order: " + order.getId()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);

        int totalAmount = order.getFinalPrice().multiply(BigDecimal.valueOf(100)).intValue();

        List<BesteronIntentRequest.Item> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItemDto item : order.getItems()) {
                items.add(BesteronIntentRequest.Item.builder()
                        .name(item.getName())
                        .type("ITEM")
                        .amount(item.getPrice().multiply(BigDecimal.valueOf(100)).intValue())
                        .count(item.getQuantity())
                        .build());
            }
        }

        BesteronIntentRequest intentRequest = BesteronIntentRequest.builder()
                .totalAmount(totalAmount)
                .currencyCode("EUR")
                .orderNumber(order.getOrderIdentifier() != null ? String.valueOf(order.getOrderIdentifier()) : order.getId())
                .language("SK")
                .paymentMethods(List.of("GIBASKBX", "POBNSKBA", "SUBASKBX", "TATRSKBX", "UNCRSKBX", "VIAMO"))
                .items(items)
                .callback(BesteronIntentRequest.Callback.builder()
                        .returnUrl(returnUrl)
                        .notificationUrl(notificationUrl)
                        .build())
                .buyer(BesteronIntentRequest.Buyer.builder()
                        .email(customer.getEmail())
                        .firstName(customer.getFirstname())
                        .lastName(customer.getLastname())
                        .build())
                .build();

        HttpEntity<BesteronIntentRequest> request = new HttpEntity<>(intentRequest, headers);

        ResponseEntity<BesteronIntentResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/payment-intent",
                request,
                BesteronIntentResponse.class
        );

        if (response.getBody() == null || response.getBody().getRedirectUrl() == null) {
            throw new PaymentException("Failed to retrieve redirect URL from Besteron");
        }

        BesteronPayment payment = new BesteronPayment();
        payment.setOrderId(order.getId());
        payment.setTransactionId(response.getBody().getTransactionId());
        payment.setRedirectUrl(response.getBody().getRedirectUrl());
        payment.setStatus("Created");
        besteronPaymentRepository.save(payment);

        return response.getBody().getRedirectUrl();
    }

    public void paymentCallback(String transactionId) {
        besteronPaymentRepository.findByTransactionId(transactionId).ifPresent(payment -> checkStatus(payment.getOrderId()));
    }

    public Optional<String> getOrderIdByTransactionId(String transactionId) {
        return besteronPaymentRepository.findByTransactionId(transactionId)
                .map(BesteronPayment::getOrderId);
    }

    private void sendOrderPaidEmail(Order order) {
        if (order.getEmail() == null || order.getEmail().isEmpty()) {
            log.warn("Cannot send 'Order Paid' email: Customer email is missing for order {}", order.getOrderIdentifier());
            return;
        }
        try {
            String template = getEmailPaidTemplate();

            String firstname = order.getFirstname() != null ? order.getFirstname() : "Customer";
            String orderIdentifier = order.getOrderIdentifier() != null ? order.getOrderIdentifier().toString() : "";
            String orderConfirmationLink = frontendUrl + "/order/confirmation/" + order.getId();

            String body = template
                    .replace("{{firstname}}", firstname)
                    .replace("{{orderIdentifier}}", orderIdentifier)
                    .replace("{{orderConfirmationLink}}", orderConfirmationLink)
                    .replace("{{currentYear}}", String.valueOf(java.time.Year.now().getValue()));

            emailService.sendEmail(order.getEmail(), "Objednávka zaplatená", body, true, null);
            log.info("Sent 'Order Paid' email for order {}", order.getOrderIdentifier());

        } catch (Exception e) {
            log.error("Failed to send 'Order Paid' email for order {}", order.getOrderIdentifier(), e);
        }
    }

    private String getEmailPaidTemplate() throws java.io.IOException {
        if (emailPaidTemplate == null) {
            ClassPathResource resource = new ClassPathResource("templates/email/order_paid.html");
            byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
            emailPaidTemplate = new String(data, StandardCharsets.UTF_8);
        }
        return emailPaidTemplate;
    }
}
