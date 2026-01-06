package sk.tany.rest.api.dto.client.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCallbackRequest {
    @JsonProperty("OPERATION")
    private String operation;
    @NotNull
    @JsonProperty("ORDERNUMBER")
    private String orderNumber;
    @JsonProperty("MERORDERNUM")
    private String merOrderNum;
    @JsonProperty("MD")
    private String md;
    @JsonProperty("PRCODE")
    private String prcode;
    @JsonProperty("SRCODE")
    private String srcode;
    @JsonProperty("RESULTTEXT")
    private String resultText;
    @JsonProperty("DIGEST")
    private String digest;
    @JsonProperty("DIGEST1")
    private String digest1;
}
