package sk.tany.rest.api.service.common;

import java.io.File;

public interface EmailService {
    void sendEmail(String to, String subject, String body, boolean isHtml, File... attachments);
}
