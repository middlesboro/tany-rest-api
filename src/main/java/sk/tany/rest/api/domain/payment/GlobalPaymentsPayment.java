package sk.tany.rest.api.domain.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GlobalPaymentsPayment extends Payment {

    private String orderId;
    private String status;
    private String resultText;
    private String description;
    private String transactionId;
    private String approvalCode;
    private String prCode;
    private String srCode;
    private String rc;
}
