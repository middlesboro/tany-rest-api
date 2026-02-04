package sk.tany.rest.api.dto.isklad;

import lombok.Data;

@Data
public class ISkladWebhookRequest {
    private ISkladWebhookAuth auth;
    private ISkladWebhookRequestData request;
}
