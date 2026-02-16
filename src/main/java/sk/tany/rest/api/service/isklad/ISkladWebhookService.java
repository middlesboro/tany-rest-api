package sk.tany.rest.api.service.isklad;

import sk.tany.rest.api.dto.isklad.ISkladOrderStatusUpdateRequest;

public interface ISkladWebhookService {
    void updateOrderStatus(ISkladOrderStatusUpdateRequest request);
}
