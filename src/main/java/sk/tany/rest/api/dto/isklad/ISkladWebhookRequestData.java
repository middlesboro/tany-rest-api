package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ISkladWebhookRequestData {
    @JsonProperty("req_method")
    private String reqMethod;
    @JsonProperty("req_data")
    private ISkladOrderStatusUpdateRequest reqData;
}
