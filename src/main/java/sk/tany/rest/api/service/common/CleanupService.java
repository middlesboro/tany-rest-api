package sk.tany.rest.api.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.auth.AuthorizationCode;
import sk.tany.rest.api.domain.auth.AuthorizationCodeRepository;
import sk.tany.rest.api.domain.auth.MagicLinkToken;
import sk.tany.rest.api.domain.auth.MagicLinkTokenRepository;
import sk.tany.rest.api.domain.payment.BesteronPayment;
import sk.tany.rest.api.domain.payment.BesteronPaymentRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final BesteronPaymentRepository besteronPaymentRepository;

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void cleanupAuthorizationCodes() {
        Instant threshold = Instant.now().minus(30, ChronoUnit.SECONDS);
        List<AuthorizationCode> toDelete = authorizationCodeRepository.findAll().stream()
                .filter(code -> code.getCreatedDate() != null && code.getCreatedDate().isBefore(threshold))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            log.info("Cleaning up {} expired AuthorizationCodes", toDelete.size());
            toDelete.forEach(authorizationCodeRepository::delete);
        }
    }

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void cleanupMagicLinkTokens() {
        Instant threshold = Instant.now().minus(300, ChronoUnit.SECONDS);
        List<MagicLinkToken> toDelete = magicLinkTokenRepository.findAll().stream()
                .filter(token -> token.getCreatedDate() != null && token.getCreatedDate().isBefore(threshold))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            log.info("Cleaning up {} expired MagicLinkTokens", toDelete.size());
            toDelete.forEach(magicLinkTokenRepository::delete);
        }
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupBesteronPayments() {
        Instant threshold = Instant.now().minus(604800, ChronoUnit.SECONDS); // 7 days
        List<BesteronPayment> toDelete = besteronPaymentRepository.findAll().stream()
                .filter(payment -> payment.getCreatedDate() != null && payment.getCreatedDate().isBefore(threshold))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            log.info("Cleaning up {} expired BesteronPayments", toDelete.size());
            toDelete.forEach(besteronPaymentRepository::delete);
        }
    }
}
