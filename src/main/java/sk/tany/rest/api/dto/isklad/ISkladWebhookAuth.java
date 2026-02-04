package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ISkladWebhookAuth {
    @JsonProperty("auth_id")
    private String authId;
    @JsonProperty("auth_key")
    private String authKey;
}
