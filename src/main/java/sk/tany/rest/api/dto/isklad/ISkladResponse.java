package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ISkladResponse<T> {
    @JsonProperty("auth_status")
    private Integer authStatus;

    @JsonProperty("auth_status_message")
    private String authStatusMessage;

    @JsonProperty("resp_code")
    private Integer respCode;

    @JsonProperty("resp_note")
    private String respNote;

    private T response;
}
