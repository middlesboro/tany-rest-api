package sk.tany.rest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GlobalPaymentDetailsDto {
    private String merchantNumber;
    private String operation;
    private String orderNumber;
    private String amount;
    private String currency;
    private String depositFlag;
    private String merOrderNum;
    private String url;
    private String paymentUrl;
    private String description = "";
    private String md;
    private String digest;
}
