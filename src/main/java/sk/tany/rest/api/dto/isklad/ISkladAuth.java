package sk.tany.rest.api.dto.isklad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ISkladAuth {
    @JsonProperty("auth_id")
    private String authId;

    @JsonProperty("auth_key")
    private String authKey;

    @JsonProperty("auth_token")
    private String authToken;
}
