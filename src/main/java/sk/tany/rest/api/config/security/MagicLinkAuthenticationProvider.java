package sk.tany.rest.api.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.auth.MagicLinkTokenState;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MagicLinkAuthenticationProvider implements AuthenticationProvider {

    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MagicLinkAuthenticationToken token = (MagicLinkAuthenticationToken) authentication;
        String exchangeToken = (String) token.getPrincipal();

        synchronized (this) {
            MagicLinkToken magicLinkToken = magicLinkTokenRepository.findByJti(exchangeToken)
                    .orElseThrow(() -> new BadCredentialsException("Invalid token"));

            if (magicLinkToken.getExpiration().isBefore(Instant.now())) {
                throw new BadCredentialsException("Token expired");
            }

            if (magicLinkToken.getState() != MagicLinkTokenState.PENDING) {
                throw new BadCredentialsException("Token already used");
            }

            // Invalidate token
            magicLinkToken.setState(MagicLinkTokenState.VERIFIED);
            magicLinkTokenRepository.save(magicLinkToken);

            Customer customer = customerRepository.findByEmail(magicLinkToken.getCustomerEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(customer.getRole() != null ? customer.getRole().name() : "USER")
            );

            return new UsernamePasswordAuthenticationToken(customer.getEmail(), null, authorities);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MagicLinkAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
