package sk.tany.rest.api.service;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.emails.Emails;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "apiToken", "test-token");
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@domain.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Test Sender");
    }

    @Test
    void sendEmail_shouldSendHtmlEmailSuccessfully() throws MailerSendException {
        try (MockedConstruction<MailerSend> mockedMailerSend = mockConstruction(MailerSend.class,
                (mock, context) -> {
                    Emails emails = mock(Emails.class);
                    when(mock.emails()).thenReturn(emails);
                    when(emails.send(any(Email.class))).thenReturn(new MailerSendResponse());
                })) {

            emailService.sendEmail("recipient@example.com", "Subject", "<h1>Body</h1>", true, null);

            MailerSend ms = mockedMailerSend.constructed().get(0);
            verify(ms).setToken("test-token");
            verify(ms.emails()).send(any(Email.class));
        }
    }

    @Test
    void sendEmail_shouldSendPlainEmailSuccessfully() throws MailerSendException {
        try (MockedConstruction<MailerSend> mockedMailerSend = mockConstruction(MailerSend.class,
                (mock, context) -> {
                    Emails emails = mock(Emails.class);
                    when(mock.emails()).thenReturn(emails);
                    when(emails.send(any(Email.class))).thenReturn(new MailerSendResponse());
                })) {

            emailService.sendEmail("recipient@example.com", "Subject", "Body", false, null);

            MailerSend ms = mockedMailerSend.constructed().get(0);
            verify(ms).setToken("test-token");
            verify(ms.emails()).send(any(Email.class));
        }
    }

    @Test
    void sendEmail_shouldSendEmailWithAttachmentSuccessfully() throws MailerSendException {
        File attachment = new File("pom.xml"); // Use a real file that exists
        try (MockedConstruction<MailerSend> mockedMailerSend = mockConstruction(MailerSend.class,
                (mock, context) -> {
                    Emails emails = mock(Emails.class);
                    when(mock.emails()).thenReturn(emails);
                    when(emails.send(any(Email.class))).thenReturn(new MailerSendResponse());
                })) {

            emailService.sendEmail("recipient@example.com", "Subject", "Body", false, attachment);

            MailerSend ms = mockedMailerSend.constructed().get(0);
            verify(ms).setToken("test-token");
            verify(ms.emails()).send(any(Email.class));
        }
    }

    @Test
    void sendEmail_shouldThrowExceptionOnMailerSendException() {
        try (MockedConstruction<MailerSend> mockedMailerSend = mockConstruction(MailerSend.class,
                (mock, context) -> {
                    Emails emails = mock(Emails.class);
                    when(mock.emails()).thenReturn(emails);
                    when(emails.send(any(Email.class))).thenThrow(new MailerSendException("Error"));
                })) {

            assertThrows(RuntimeException.class, () ->
                    emailService.sendEmail("recipient@example.com", "Subject", "Body", false, null));
        }
    }
}
