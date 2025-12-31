package sk.tany.rest.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sk.tany.rest.api.component.JwtUtil;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.auth.MagicLinkTokenState;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.exception.InvalidTokenException;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private MagicLinkTokenRepository magicLinkTokenRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void initiateLogin_CreatesCustomerAndTokenAndSendsEmail() {
        String email = "test@example.com";
        String token = "jwt-token";
        ReflectionTestUtils.setField(authenticationService, "baseUrl", "http://localhost:8080");

        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(jwtUtil.generateMagicLinkToken(anyString())).thenReturn(token);

        authenticationService.initiateLogin(email);

        verify(customerRepository).save(any(Customer.class));
        verify(magicLinkTokenRepository).save(any(MagicLinkToken.class));
        verify(emailService).sendEmail(eq(email), eq("Login Verification"), contains(token), eq(false), isNull());
    }

    @Test
    void verifyLogin_ValidToken_ReturnsSessionToken() {
        String token = "valid-token";
        String jti = "uuid-jti";
        String email = "test@example.com";
        String sessionToken = "session-token";

        MagicLinkToken magicLinkToken = new MagicLinkToken();
        magicLinkToken.setJti(jti);
        magicLinkToken.setCustomerEmail(email);
        magicLinkToken.setState(MagicLinkTokenState.PENDING);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.hasClaim(token, "magic_link", true)).thenReturn(true);
        when(jwtUtil.extractJti(token)).thenReturn(jti);
        when(magicLinkTokenRepository.findByJti(jti)).thenReturn(Optional.of(magicLinkToken));
        when(jwtUtil.generateSessionToken(email)).thenReturn(sessionToken);

        String result = authenticationService.verifyLogin(token);

        assertEquals(sessionToken, result);
        assertEquals(MagicLinkTokenState.VERIFIED, magicLinkToken.getState());
        verify(magicLinkTokenRepository).save(magicLinkToken);
    }

    @Test
    void verifyLogin_InvalidToken_ThrowsException() {
        when(jwtUtil.validateToken("invalid")).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> authenticationService.verifyLogin("invalid"));
    }

    @Test
    void verifyLogin_VerifiedToken_ThrowsException() {
        String token = "valid-token";
        String jti = "uuid-jti";
        MagicLinkToken magicLinkToken = new MagicLinkToken();
        magicLinkToken.setState(MagicLinkTokenState.VERIFIED);

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.hasClaim(token, "magic_link", true)).thenReturn(true);
        when(jwtUtil.extractJti(token)).thenReturn(jti);
        when(magicLinkTokenRepository.findByJti(jti)).thenReturn(Optional.of(magicLinkToken));

        assertThrows(InvalidTokenException.class, () -> authenticationService.verifyLogin(token));
    }
}
