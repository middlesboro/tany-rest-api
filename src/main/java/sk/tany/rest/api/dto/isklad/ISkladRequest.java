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
public class ISkladRequest<T> {
    private ISkladAuth auth;

    @JsonProperty("request")
    private RequestData<T> request;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestData<T> {
        @JsonProperty("req_method")
        private String reqMethod;

        @JsonProperty("req_data")
        private T reqData;
    }
}
