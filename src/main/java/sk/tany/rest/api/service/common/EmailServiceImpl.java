package sk.tany.rest.api.service.common;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${mailersend.api-token}")
    private String apiToken;

    @Value("${mailersend.from-email}")
    private String fromEmail;

    @Value("${mailersend.from-name}")
    private String fromName;

    @Override
    public void sendEmail(String to, String subject, String body, boolean isHtml, File attachment) {
        Email email = new Email();

        email.setFrom(fromName, fromEmail);
        email.addRecipient("", to);
        email.setSubject(subject);

        if (isHtml) {
            email.setHtml(body);
        } else {
            email.setPlain(body);
        }

        if (attachment != null) {
            try {
                email.attachFile(attachment);
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to attach file", e);
            }
        }

        MailerSend ms = new MailerSend();
        ms.setToken(apiToken);

        try {
            MailerSendResponse response = ms.emails().send(email);
            // Optionally log the response.messageId
        } catch (MailerSendException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
