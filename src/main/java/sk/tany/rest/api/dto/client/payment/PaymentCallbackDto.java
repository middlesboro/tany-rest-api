package sk.tany.rest.api.dto.client.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCallbackDto {

    private String orderId;
    private String operation;
    private String orderNumber;
    private String merOrderNum;
    private String md;
    private String prCode;
    private String srCode;
    private String resultText;
    private String digest;
    private String digest1;

}
