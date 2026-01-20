package sk.tany.rest.api.domain.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BesteronPayment extends Payment {

    private String orderId;
    private String status;
    private String originalStatus; // Added
    private String resultText;
    private String transactionId;
    private String ammount;
    private String currency;
    private String payer;
    private String sign;
    private String redirectUrl; // Added
}
