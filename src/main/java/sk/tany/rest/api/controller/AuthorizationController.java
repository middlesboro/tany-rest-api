package sk.tany.rest.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.auth.MagicLinkTokenState;
import sk.tany.rest.api.domain.customer.CustomerRepository;
import sk.tany.rest.api.service.common.EmailService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/auth/magic-link")
@RequiredArgsConstructor
public class AuthorizationController {

    private final CustomerRepository customerRepository;
    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final EmailService emailService;

    @Value("${eshop.frontend-url}")
    private String frontendUrl;

    @PostMapping("/request")
    public ResponseEntity<Void> requestMagicLink(@RequestParam String email) {
        if (customerRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String exchangeToken = UUID.randomUUID().toString();
        MagicLinkToken token = new MagicLinkToken();
        token.setId(UUID.randomUUID().toString());
        token.setJti(exchangeToken);
        token.setCustomerEmail(email);
        token.setState(MagicLinkTokenState.PENDING);
        token.setCreatedDate(Instant.now());
        token.setExpiration(Instant.now().plus(5, ChronoUnit.MINUTES));

        magicLinkTokenRepository.save(token);

        String link = frontendUrl + "/magic-link?token=" + exchangeToken;
        String body = "<p>Click here to login: <a href=\"" + link + "\">" + link + "</a></p>";
        emailService.sendEmail(email, "Magic Link Login", body, true, null);

        return ResponseEntity.ok().build();
    }
}
