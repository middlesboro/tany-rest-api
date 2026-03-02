package sk.tany.rest.api.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.mailplatform.MailPlatform;
import sk.tany.rest.api.domain.mailplatform.MailPlatformRepository;
import sk.tany.rest.api.domain.mailplatform.MailPlatformType;
import sk.tany.rest.api.exception.EmailException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingEmailServiceTest {

    @Mock
    private MailPlatformRepository mailPlatformRepository;

    @Mock
    private PlatformEmailService mailerSendService;

    @Mock
    private PlatformEmailService brevoService;

    private RoutingEmailService routingEmailService;

    @BeforeEach
    void setUp() {
        when(mailerSendService.getPlatformType()).thenReturn(MailPlatformType.MAILER_SEND);
        when(brevoService.getPlatformType()).thenReturn(MailPlatformType.BREVO);

        routingEmailService = new RoutingEmailService(
                mailPlatformRepository,
                Arrays.asList(mailerSendService, brevoService)
        );
    }

    @Test
    void testSendEmail_NoActivePlatforms() {
        when(mailPlatformRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        assertThrows(EmailException.class, () ->
                routingEmailService.sendEmail("to@test.com", "Sub", "Body", false)
        );
    }

    @Test
    void testSendEmail_UsesPlatformWithHighestLimitRemaining() {
        MailPlatform p1 = new MailPlatform();
        p1.setPlatformType(MailPlatformType.MAILER_SEND);
        p1.setLimitPerDay(100);
        p1.setSentPerDay(90); // 10 remaining
        p1.setLimitPerMonth(1000);
        p1.setSentPerMonth(900);
        p1.setLastSentDate(LocalDate.now());

        MailPlatform p2 = new MailPlatform();
        p2.setPlatformType(MailPlatformType.BREVO);
        p2.setLimitPerDay(100);
        p2.setSentPerDay(50); // 50 remaining -> should be chosen
        p2.setLimitPerMonth(1000);
        p2.setSentPerMonth(500);
        p2.setLastSentDate(LocalDate.now());

        when(mailPlatformRepository.findByActiveTrue()).thenReturn(Arrays.asList(p1, p2));

        routingEmailService.sendEmail("to@test.com", "Sub", "Body", false);

        verify(brevoService).sendEmail("to@test.com", "Sub", "Body", false, new java.io.File[0]);
        verify(mailerSendService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), any());

        ArgumentCaptor<MailPlatform> captor = ArgumentCaptor.forClass(MailPlatform.class);
        verify(mailPlatformRepository).save(captor.capture());

        MailPlatform saved = captor.getValue();
        assertEquals(MailPlatformType.BREVO, saved.getPlatformType());
        assertEquals(51, saved.getSentPerDay());
        assertEquals(501, saved.getSentPerMonth());
    }

    @Test
    void testSendEmail_LimitsResetIfNewDay() {
        MailPlatform p1 = new MailPlatform();
        p1.setPlatformType(MailPlatformType.MAILER_SEND);
        p1.setLimitPerDay(100);
        p1.setSentPerDay(100);
        p1.setLimitPerMonth(1000);
        p1.setSentPerMonth(100);
        p1.setLastSentDate(LocalDate.now().minusDays(1)); // yesterday

        when(mailPlatformRepository.findByActiveTrue()).thenReturn(Collections.singletonList(p1));

        routingEmailService.sendEmail("to@test.com", "Sub", "Body", false);

        verify(mailerSendService).sendEmail("to@test.com", "Sub", "Body", false, new java.io.File[0]);

        // One save for reset, one save for actual send
        verify(mailPlatformRepository, times(2)).save(p1);

        assertEquals(1, p1.getSentPerDay());
        assertEquals(101, p1.getSentPerMonth()); // Assumes same month
        assertEquals(LocalDate.now(), p1.getLastSentDate());
    }
}
