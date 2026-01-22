package sk.tany.rest.api.service.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.tany.rest.api.component.JwtUtil;
import sk.tany.rest.api.domain.auth.AuthorizationCode;
import sk.tany.rest.api.domain.auth.AuthorizationCodeRepository;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.auth.MagicLinkTokenState;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.exception.AuthenticationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Value("${eshop.base-url}")
    private String frontendUrl;

    @Override
    public void initiateLogin(String email) {
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);
        if (customerOptional.isEmpty()) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customerRepository.save(customer);
        }

        String jti = UUID.randomUUID().toString();
        MagicLinkToken tokenEntity = new MagicLinkToken();
        tokenEntity.setJti(jti);
        tokenEntity.setCustomerEmail(email);
        tokenEntity.setState(MagicLinkTokenState.PENDING);
        tokenEntity.setExpiration(Instant.now().plus(15, ChronoUnit.MINUTES));
        tokenEntity.setCreatedDate(Instant.now());
        magicLinkTokenRepository.save(tokenEntity);

        String token = jwtUtil.generateMagicLinkToken(jti);

        String link = frontendUrl + "/api/login/verify?token=" + token; // Using backend endpoint which redirects
        String body = "<p>Click here to login: <a href=\"" + link + "\">" + link + "</a></p>";
        emailService.sendEmail(email, "Login to Tany.sk", body, true, null);
    }

    @Override
    public String verifyAndGenerateCode(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new AuthenticationException.InvalidToken("Invalid token");
        }

        if (!jwtUtil.hasClaim(token, "magic_link", true)) {
            throw new AuthenticationException.InvalidToken("Invalid token type.");
        }

        String jti = jwtUtil.extractJti(token);
        MagicLinkToken magicLinkToken = magicLinkTokenRepository.findByJti(jti)
                .orElseThrow(() -> new AuthenticationException.InvalidToken("Token not found."));

        if (magicLinkToken.getExpiration().isBefore(Instant.now())) {
            throw new AuthenticationException.InvalidToken("Token expired.");
        }

        if (magicLinkToken.getState() != MagicLinkTokenState.PENDING) {
            throw new AuthenticationException.InvalidToken("Token already used.");
        }

        magicLinkToken.setState(MagicLinkTokenState.VERIFIED);
        magicLinkTokenRepository.save(magicLinkToken);

        String email = magicLinkToken.getCustomerEmail();
        // Generate Authorization Code
        String code = UUID.randomUUID().toString();
        AuthorizationCode authCode = new AuthorizationCode();
        authCode.setCode(code);
        authCode.setEmail(email);
        authCode.setExpiration(Instant.now().plus(5, ChronoUnit.MINUTES));
        authCode.setCreatedDate(Instant.now());
        authorizationCodeRepository.save(authCode);

        return code;
    }

    @Override
    public String exchangeCode(String code) {
        Optional<AuthorizationCode> authCodeOpt = authorizationCodeRepository.findAll().stream()
                .filter(ac -> ac.getCode().equals(code))
                .findFirst();

        if (authCodeOpt.isEmpty()) {
            throw new AuthenticationException.AuthorizationFailed("Invalid authorization code");
        }

        AuthorizationCode authCode = authCodeOpt.get();
        if (authCode.getExpiration().isBefore(Instant.now())) {
            throw new AuthenticationException.AuthorizationFailed("Authorization code expired");
        }

        authorizationCodeRepository.delete(authCode);

        // Return the JWT associated with this code if available, or create new one
        if (authCode.getJwt() != null) {
            return authCode.getJwt();
        }

        Customer customer = customerRepository.findByEmail(authCode.getEmail())
             .orElseThrow(() -> new AuthenticationException.AuthorizationFailed("Customer not found"));

        List<String> roles = List.of(customer.getRole() != null ? customer.getRole().name() : "USER");
        return jwtUtil.generateSessionToken(customer.getEmail(), roles);
    }
}
