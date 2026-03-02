package sk.tany.rest.api.service.common;

import sk.tany.rest.api.domain.mailplatform.MailPlatformType;

public interface PlatformEmailService extends EmailService {
    MailPlatformType getPlatformType();
}
