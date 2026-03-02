package sk.tany.rest.api.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.mailplatform.MailPlatform;
import sk.tany.rest.api.domain.mailplatform.MailPlatformRepository;
import sk.tany.rest.api.domain.mailplatform.MailPlatformType;
import sk.tany.rest.api.exception.EmailException;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Primary
@Slf4j
public class RoutingEmailService implements EmailService {

    private final MailPlatformRepository mailPlatformRepository;
    private final Map<MailPlatformType, PlatformEmailService> emailServices;

    public RoutingEmailService(MailPlatformRepository mailPlatformRepository, List<PlatformEmailService> services) {
        this.mailPlatformRepository = mailPlatformRepository;
        this.emailServices = services.stream()
                .collect(Collectors.toMap(PlatformEmailService::getPlatformType, s -> s));
    }

    @Override
    public void sendEmail(String to, String subject, String body, boolean isHtml, File... attachments) {
        List<MailPlatform> activePlatforms = mailPlatformRepository.findByActiveTrue();

        if (activePlatforms.isEmpty()) {
            throw new EmailException("No active email platforms found to send the email.");
        }

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        int emailsToSend = 1;
        if (to != null && to.contains(",")) {
            emailsToSend = to.split(",").length;
        }

        for (MailPlatform platform : activePlatforms) {
            boolean changed = false;

            if (platform.getLastSentDate() != null) {
                if (platform.getLastSentDate().isBefore(today)) {
                    platform.setSentPerDay(0);
                    changed = true;
                }
                if (!YearMonth.from(platform.getLastSentDate()).equals(currentMonth)) {
                    platform.setSentPerMonth(0);
                    changed = true;
                }
            } else {
                platform.setSentPerDay(0);
                platform.setSentPerMonth(0);
                changed = true;
            }

            if (changed) {
                mailPlatformRepository.save(platform);
            }
        }

        final int count = emailsToSend;

        Optional<MailPlatform> selectedPlatformOpt = activePlatforms.stream()
                .filter(p -> (p.getSentPerDay() + count) <= p.getLimitPerDay() &&
                             (p.getSentPerMonth() + count) <= p.getLimitPerMonth())
                .max(Comparator.comparingInt(p -> (p.getLimitPerDay() - p.getSentPerDay())));

        if (!selectedPlatformOpt.isPresent()) {
            throw new EmailException("All active email platforms have reached their limits.");
        }

        MailPlatform platformToUse = selectedPlatformOpt.get();
        PlatformEmailService serviceToUse = emailServices.get(platformToUse.getPlatformType());

        if (serviceToUse == null) {
            throw new EmailException("No implementation found for email platform type: " + platformToUse.getPlatformType());
        }

        try {
            serviceToUse.sendEmail(to, subject, body, isHtml, attachments);

            platformToUse.setSentPerDay(platformToUse.getSentPerDay() + count);
            platformToUse.setSentPerMonth(platformToUse.getSentPerMonth() + count);
            platformToUse.setLastSentDate(today);
            mailPlatformRepository.save(platformToUse);

            log.info("Email sent successfully using platform: {}", platformToUse.getName());
        } catch (Exception e) {
            log.error("Failed to send email via platform {}", platformToUse.getName(), e);
            throw new EmailException("Failed to send email via routing service", e);
        }
    }
}
