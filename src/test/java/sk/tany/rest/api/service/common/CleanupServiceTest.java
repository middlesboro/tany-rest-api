package sk.tany.rest.api.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.auth.AuthorizationCode;
import sk.tany.rest.api.domain.auth.AuthorizationCodeRepository;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.domain.order.OrderRepository;
import sk.tany.rest.api.domain.payment.BesteronPayment;
import sk.tany.rest.api.domain.payment.BesteronPaymentRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CleanupServiceTest {

    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;
    @Mock
    private MagicLinkTokenRepository magicLinkTokenRepository;
    @Mock
    private BesteronPaymentRepository besteronPaymentRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private OrderRepository orderRepository;

    private CleanupService cleanupService;

    @BeforeEach
    void setUp() {
        cleanupService = new CleanupService(authorizationCodeRepository, magicLinkTokenRepository, besteronPaymentRepository, cartRepository, orderRepository);
    }

    @Test
    void cleanupAuthorizationCodes_ShouldDeleteExpiredCodes() {
        AuthorizationCode expired = new AuthorizationCode();
        expired.setCode("expired");
        expired.setCreateDate(Instant.now().minus(100, ChronoUnit.DAYS));

        AuthorizationCode active = new AuthorizationCode();
        active.setCode("active");
        active.setCreateDate(Instant.now().plus(1, ChronoUnit.DAYS));

        when(authorizationCodeRepository.findAll()).thenReturn(List.of(expired, active));

        cleanupService.cleanupAuthorizationCodes();

        verify(authorizationCodeRepository).delete(expired);
        verify(authorizationCodeRepository, never()).delete(active);
    }

    @Test
    void cleanupMagicLinkTokens_ShouldDeleteExpiredTokens() {
        MagicLinkToken expired = new MagicLinkToken();
        expired.setJti("expired");
        expired.setCreateDate(Instant.now().minus(100, ChronoUnit.DAYS));

        MagicLinkToken active = new MagicLinkToken();
        active.setJti("active");
        active.setCreateDate(Instant.now().plus(1, ChronoUnit.DAYS));

        when(magicLinkTokenRepository.findAll()).thenReturn(List.of(expired, active));

        cleanupService.cleanupMagicLinkTokens();

        verify(magicLinkTokenRepository).delete(expired);
        verify(magicLinkTokenRepository, never()).delete(active);
    }

    @Test
    void cleanupBesteronPayments_ShouldDeleteExpiredPayments() {
        BesteronPayment expired = new BesteronPayment();
        expired.setTransactionId("expired");
        expired.setCreateDate(Instant.now().minus(100, ChronoUnit.DAYS));

        BesteronPayment active = new BesteronPayment();
        active.setTransactionId("active");
        active.setCreateDate(Instant.now().plus(1, ChronoUnit.DAYS));

        when(besteronPaymentRepository.findAll()).thenReturn(List.of(expired, active));

        cleanupService.cleanupBesteronPayments();

        verify(besteronPaymentRepository).delete(expired);
        verify(besteronPaymentRepository, never()).delete(active);
    }

    @Test
    void cleanupCarts_ShouldDeleteExpiredCartsWithoutOrders() {
        Cart oldCartWithoutOrder = new Cart();
        oldCartWithoutOrder.setId("1");
        oldCartWithoutOrder.setCartId("1");
        oldCartWithoutOrder.setCreateDate(Instant.now().minus(100, ChronoUnit.DAYS));

        Cart oldCartWithOrder = new Cart();
        oldCartWithOrder.setId("2");
        oldCartWithOrder.setCartId("2");
        oldCartWithOrder.setCreateDate(Instant.now().minus(100, ChronoUnit.DAYS));

        Cart newCart = new Cart();
        newCart.setId("3");
        newCart.setCartId("3");
        newCart.setCreateDate(Instant.now().plus(1, ChronoUnit.DAYS));

        sk.tany.rest.api.domain.order.Order order = new sk.tany.rest.api.domain.order.Order();
        order.setCartId("2");

        when(cartRepository.findAll()).thenReturn(List.of(oldCartWithoutOrder, oldCartWithOrder, newCart));
        when(orderRepository.findAll()).thenReturn(List.of(order));

        cleanupService.cleanupCarts();

        verify(cartRepository).delete(oldCartWithoutOrder);
        verify(cartRepository, never()).delete(oldCartWithOrder);
        verify(cartRepository, never()).delete(newCart);
    }
}
