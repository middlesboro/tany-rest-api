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
public class ISkladInvoice {
    @JsonProperty("invoice_id")
    private Integer invoiceId;

    @JsonProperty("invoice_date")
    private String invoiceDate;
}
