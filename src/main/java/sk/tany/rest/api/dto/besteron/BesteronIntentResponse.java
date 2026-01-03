package sk.tany.rest.api.dto.besteron;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BesteronIntentResponse {
    private String redirectUrl;
    private String transactionId;
}
