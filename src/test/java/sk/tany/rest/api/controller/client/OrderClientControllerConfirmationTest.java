package sk.tany.rest.api.controller.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.component.SecurityUtil;
import sk.tany.rest.api.dto.OrderDto;
import sk.tany.rest.api.dto.client.order.get.OrderClientGetResponse;
import sk.tany.rest.api.exception.AuthenticationException;
import sk.tany.rest.api.mapper.OrderClientApiMapper;
import sk.tany.rest.api.service.client.OrderClientService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OrderClientControllerConfirmationTest {

    @Mock
    private OrderClientService orderClientService;
    @Mock
    private OrderClientApiMapper orderClientApiMapper;
    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private OrderClientController orderClientController;

    private OrderDto order;

    @BeforeEach
    void setUp() {
        order = new OrderDto();
        order.setId("order-123");
        order.setCustomerId("user-1");
    }

    @Test
    void shouldAllowAccessWithin20MinutesWithoutAuthentication() {
        order.setCreateDate(Instant.now().minus(10, ChronoUnit.MINUTES));

        when(orderClientService.getOrder("order-123")).thenReturn(order);
        when(orderClientApiMapper.toGetResponse(order)).thenReturn(new OrderClientGetResponse());

        OrderClientGetResponse response = orderClientController.getOrderConfirmation("order-123");

        assertNotNull(response);
        verify(securityUtil, never()).getLoggedInUserId();
    }

    @Test
    void shouldDenyAccessAfter20MinutesWithoutAuthentication() {
        order.setCreateDate(Instant.now().minus(21, ChronoUnit.MINUTES));

        when(orderClientService.getOrder("order-123")).thenReturn(order);
        when(securityUtil.getLoggedInUserId()).thenReturn(null);

        assertThrows(AuthenticationException.InvalidToken.class, () ->
            orderClientController.getOrderConfirmation("order-123")
        );
    }

    @Test
    void shouldAllowAccessAfter20MinutesWithCorrectAuthentication() {
        order.setCreateDate(Instant.now().minus(30, ChronoUnit.MINUTES));

        when(orderClientService.getOrder("order-123")).thenReturn(order);
        when(securityUtil.getLoggedInUserId()).thenReturn("user-1");
        when(orderClientApiMapper.toGetResponse(order)).thenReturn(new OrderClientGetResponse());

        OrderClientGetResponse response = orderClientController.getOrderConfirmation("order-123");

        assertNotNull(response);
    }

    @Test
    void shouldDenyAccessAfter20MinutesWithIncorrectAuthentication() {
        order.setCreateDate(Instant.now().minus(30, ChronoUnit.MINUTES));

        when(orderClientService.getOrder("order-123")).thenReturn(order);
        when(securityUtil.getLoggedInUserId()).thenReturn("user-2"); // Different user

        assertThrows(AuthenticationException.InvalidToken.class, () ->
            orderClientController.getOrderConfirmation("order-123")
        );
    }

    @Test
    void shouldDenyAccessJustOver20MinutesWithoutAuthentication() {
        // 20 minutes and 1 second ago
        order.setCreateDate(Instant.now().minus(20, ChronoUnit.MINUTES).minus(1, ChronoUnit.SECONDS));

        when(orderClientService.getOrder("order-123")).thenReturn(order);
        when(securityUtil.getLoggedInUserId()).thenReturn(null);

        assertThrows(AuthenticationException.InvalidToken.class, () ->
            orderClientController.getOrderConfirmation("order-123")
        );
    }

    @Test
    void shouldDenyAccessToGuestOrderAfter20MinutesEvenIfAuthenticated() {
        order.setCustomerId(null); // Guest order
        order.setCreateDate(Instant.now().minus(30, ChronoUnit.MINUTES));

        when(orderClientService.getOrder("order-123")).thenReturn(order);
        when(securityUtil.getLoggedInUserId()).thenReturn("some-user-id");

        assertThrows(AuthenticationException.InvalidToken.class, () ->
            orderClientController.getOrderConfirmation("order-123")
        );
    }
}
