package sk.tany.rest.api.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.auth.MagicLinkTokenState;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.customer.Role;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MagicLinkSecurityTest {

    @Mock
    private MagicLinkTokenRepository magicLinkTokenRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private MagicLinkAuthenticationProvider provider;

    @Test
    public void testAuthenticateSuccess() {
        String tokenStr = "valid-token";
        MagicLinkToken token = new MagicLinkToken();
        token.setJti(tokenStr);
        token.setCustomerEmail("test@test.com");
        token.setState(MagicLinkTokenState.PENDING);
        token.setExpiration(Instant.now().plus(5, ChronoUnit.MINUTES));

        Customer customer = new Customer();
        customer.setEmail("test@test.com");
        customer.setRole(Role.CUSTOMER);

        when(magicLinkTokenRepository.findByJti(tokenStr)).thenReturn(Optional.of(token));
        when(customerRepository.findByEmail("test@test.com")).thenReturn(Optional.of(customer));

        MagicLinkAuthenticationToken authReq = new MagicLinkAuthenticationToken(tokenStr);
        Authentication result = provider.authenticate(authReq);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertInstanceOf(Customer.class, result.getPrincipal());
        assertEquals("test@test.com", ((Customer) result.getPrincipal()).getEmail());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CUSTOMER")));

        verify(magicLinkTokenRepository).save(argThat(t -> t.getState() == MagicLinkTokenState.VERIFIED));
    }

    @Test
    public void testAuthenticateExpired() {
        String tokenStr = "expired-token";
        MagicLinkToken token = new MagicLinkToken();
        token.setJti(tokenStr);
        token.setExpiration(Instant.now().minus(5, ChronoUnit.MINUTES));

        when(magicLinkTokenRepository.findByJti(tokenStr)).thenReturn(Optional.of(token));

        MagicLinkAuthenticationToken authReq = new MagicLinkAuthenticationToken(tokenStr);

        assertThrows(BadCredentialsException.class, () -> provider.authenticate(authReq));
    }

}
