package sk.tany.rest.api.service.common;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.config.MailerSendConfig;
import sk.tany.rest.api.exception.EmailException;

import java.io.File;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final MailerSendConfig mailerSendConfig;

    @Override
    public void sendEmail(String to, String subject, String body, boolean isHtml, File... attachments) {
        Email email = new Email();

        email.setFrom(mailerSendConfig.getFromName(), mailerSendConfig.getFromEmail());
        email.addRecipient("", to);

        if (subject != null && !subject.endsWith(" - Tany.sk")) {
            subject = subject + " - Tany.sk";
        }
        email.setSubject(subject);

        if (isHtml) {
            email.setHtml(body);
        } else {
            email.setPlain(body);
        }

        if (attachments != null) {
            for (File attachment : attachments) {
                if (attachment != null) {
                    try {
                        email.attachFile(attachment);
                    } catch (java.io.IOException e) {
                        throw new EmailException("Failed to attach file", e);
                    }
                }
            }
        }

        MailerSend ms = new MailerSend();
        ms.setToken(mailerSendConfig.getApiToken());

        try {
            MailerSendResponse response = ms.emails().send(email);
            // Optionally log the response.messageId
        } catch (MailerSendException e) {
            throw new EmailException("Failed to send email", e);
        }
    }
}
