package sk.tany.rest.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.component.JwtUtil;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.auth.MagicLinkTokenState;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.exception.InvalidTokenException;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final CustomerRepository customerRepository;
    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${eshop.base-url}")
    private String baseUrl;

    @Override
    public void initiateLogin(String email) {
        // Find or create customer
        Optional<Customer> existingCustomer = customerRepository.findByEmail(email);

        if (existingCustomer.isEmpty()) {
            Customer newCustomer = new Customer();
            newCustomer.setEmail(email);
            customerRepository.save(newCustomer);
        }

        // Generate JTI
        String jti = UUID.randomUUID().toString();

        // Create Magic Link Token in DB
        MagicLinkToken tokenEntity = new MagicLinkToken();
        tokenEntity.setJti(jti);
        tokenEntity.setCustomerEmail(email);
        tokenEntity.setState(MagicLinkTokenState.PENDING);
        tokenEntity.setCreatedAt(new Date());
        magicLinkTokenRepository.save(tokenEntity);

        // Generate JWT
        String token = jwtUtil.generateMagicLinkToken(jti);

        // Send Email
        String magicLink = baseUrl + "/api/login/verify?token=" + token;
        String emailBody = "Click on the following link to log in: " + magicLink;

        emailService.sendEmail(email, "Login Verification", emailBody, false, null);
    }

    @Override
    public String verifyLogin(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new InvalidTokenException("Invalid token");
        }

        if (!jwtUtil.hasClaim(token, "magic_link", true)) {
            throw new InvalidTokenException("Invalid token type");
        }

        String jti = jwtUtil.extractJti(token);

        MagicLinkToken magicLinkToken = magicLinkTokenRepository.findByJti(jti)
                .orElseThrow(() -> new InvalidTokenException("Token not found or expired"));

        if (magicLinkToken.getState() == MagicLinkTokenState.VERIFIED) {
            throw new InvalidTokenException("Token already used");
        }

        // Update state
        magicLinkToken.setState(MagicLinkTokenState.VERIFIED);
        magicLinkTokenRepository.save(magicLinkToken);

        // Generate Session Token
        return jwtUtil.generateSessionToken(magicLinkToken.getCustomerEmail());
    }
}
