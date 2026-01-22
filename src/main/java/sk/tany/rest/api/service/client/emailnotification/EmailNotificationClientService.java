package sk.tany.rest.api.service.client.emailnotification;

import sk.tany.rest.api.dto.client.emailnotification.EmailNotificationCreateRequest;

public interface EmailNotificationClientService {
    void createNotification(EmailNotificationCreateRequest request);
}
