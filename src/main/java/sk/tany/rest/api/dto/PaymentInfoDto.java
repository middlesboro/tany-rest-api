package sk.tany.rest.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInfoDto {
    private String qrCode; // Base64 encoded image
    private String paymentLink;
    private String variableSymbol;
    private String iban;
    private String swift;
    private GlobalPaymentDetailsDto globalPaymentDetails;
}
