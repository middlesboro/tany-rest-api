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
import sk.tany.rest.api.domain.payment.PaymentType;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.OrderItemDto;
import sk.tany.rest.api.dto.PaymentDto;
import sk.tany.rest.api.dto.PaymentInfoDto;
import sk.tany.rest.api.dto.besteron.BesteronIntentRequest;
import sk.tany.rest.api.dto.besteron.BesteronIntentResponse;
import sk.tany.rest.api.dto.besteron.BesteronTokenResponse;
import sk.tany.rest.api.service.client.payment.PaymentTypeService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BesteronPaymentTypeService implements PaymentTypeService {

    private final RestTemplate restTemplate;
    private final CustomerRepository customerRepository;

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
            throw new RuntimeException("Failed to generate Besteron payment link", e);
        }
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
            throw new IllegalStateException("Failed to retrieve access token from Besteron");
        }

        return response.getBody().getAccessToken();
    }

    private String createPaymentIntent(OrderDto order, String token) {
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new IllegalStateException("Customer not found for order: " + order.getId()));

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
            throw new IllegalStateException("Failed to retrieve redirect URL from Besteron");
        }

        return response.getBody().getRedirectUrl();
    }
}
