package sk.tany.rest.api.service;

import java.io.File;

public interface EmailService {
    void sendEmail(String to, String subject, String body, boolean isHtml, File attachment);
}
