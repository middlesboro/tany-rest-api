package sk.tany.rest.api.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.config.BrevoConfig;
import sk.tany.rest.api.domain.mailplatform.MailPlatformType;
import sk.tany.rest.api.exception.EmailException;

import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailAttachment;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service("brevoEmailService")
@RequiredArgsConstructor
@Slf4j
public class BrevoEmailService implements PlatformEmailService {

    private final BrevoConfig brevoConfig;

    @Override
    public MailPlatformType getPlatformType() {
        return MailPlatformType.BREVO;
    }

    @Override
    public void sendEmail(String to, String subject, String body, boolean isHtml, File... attachments) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(brevoConfig.getApiKey());

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(brevoConfig.getFromEmail());
        sender.setName(brevoConfig.getFromName());
        sendSmtpEmail.setSender(sender);

        SendSmtpEmailTo sendTo = new SendSmtpEmailTo();
        sendTo.setEmail(to);
        sendSmtpEmail.setTo(Collections.singletonList(sendTo));

        if (subject != null && !subject.endsWith(" - Tany.sk")) {
            subject = subject + " - Tany.sk";
        }
        sendSmtpEmail.setSubject(subject);

        if (isHtml) {
            sendSmtpEmail.setHtmlContent(body);
        } else {
            sendSmtpEmail.setTextContent(body);
        }

        if (attachments != null && attachments.length > 0) {
            List<SendSmtpEmailAttachment> emailAttachments = new ArrayList<>();
            for (File file : attachments) {
                if (file != null && file.exists()) {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        SendSmtpEmailAttachment attachment = new SendSmtpEmailAttachment();
                        attachment.setName(file.getName());
                        attachment.setContent(fileContent);
                        emailAttachments.add(attachment);
                    } catch (Exception e) {
                        throw new EmailException("Failed to attach file for Brevo: " + file.getName(), e);
                    }
                }
            }
            if (!emailAttachments.isEmpty()) {
                sendSmtpEmail.setAttachment(emailAttachments);
            }
        }

        try {
            CreateSmtpEmail response = apiInstance.sendTransacEmail(sendSmtpEmail);
            log.info("Brevo email sent successfully with message ID: {}", response.getMessageId());
        } catch (Exception e) {
            log.error("Failed to send Brevo email", e);
            throw new EmailException("Failed to send email via Brevo", e);
        }
    }
}
