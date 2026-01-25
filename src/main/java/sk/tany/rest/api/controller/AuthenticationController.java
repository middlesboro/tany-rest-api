package sk.tany.rest.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.auth.MagicLinkTokenState;
import sk.tany.rest.api.domain.customer.Customer;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.domain.customer.Role;
import sk.tany.rest.api.service.common.EmailService;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth/magic-link")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final CustomerRepository customerRepository;
    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final EmailService emailService;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;
    @Value("${eshop.frontend-admin-url}")
    private String frontendAdminUrl;

    private final Map<String, Instant> rateLimitMap = new ConcurrentHashMap<>();

    @PostMapping("/request")
    public ResponseEntity<Void> requestMagicLink(@RequestParam String email) {
        // Rate Limiting: 1 request per minute per email
        Instant lastRequest = rateLimitMap.get(email);
        if (lastRequest != null && lastRequest.isAfter(Instant.now().minus(1, ChronoUnit.MINUTES))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        rateLimitMap.put(email, Instant.now());

        Optional<Customer> customerOptional = customerRepository.findByEmail(email);
        if (customerOptional.isPresent()) {
            String exchangeToken = UUID.randomUUID().toString();
            MagicLinkToken token = new MagicLinkToken();
            token.setId(UUID.randomUUID().toString());
            token.setJti(exchangeToken);
            token.setCustomerEmail(email);
            token.setState(MagicLinkTokenState.PENDING);
            token.setCreatedDate(Instant.now());
            token.setExpiration(Instant.now().plus(5, ChronoUnit.MINUTES));
            magicLinkTokenRepository.save(token);

            Customer customer = customerOptional.get();
            String baseUrl = customer.getRole() == Role.ADMIN ? frontendAdminUrl : frontendUrl;
            String link = baseUrl + "/magic-link?token=" + exchangeToken;

            String body = loadTemplate().replace("{{link}}", link);
            emailService.sendEmail(email, "Odkaz pre prihlásenie", body, true, null);
        }

        // Always return 200 OK to prevent email enumeration
        return ResponseEntity.ok().build();
    }

    private String loadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/magic_link.html");
            byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load magic link template", e);
            return "<p>Click here to login: <a href=\"{{link}}\">{{link}}</a></p>";
        }
    }
}
