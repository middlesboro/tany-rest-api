package sk.tany.rest.api.domain.payment;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Document(collection = "global_payments_payments")
public class GlobalPaymentsPayment {

    @Id
    private String id;

    @Field("order_id")
    private String orderId;

    @Field("merchant_order_number")
    private String merchantOrderNumber;

    @Field("pr_code")
    private String prCode;

    @Field("sr_code")
    private String srCode;

    @Field("result_text")
    private String resultText;

    @Field("digest")
    private String digest;

    @Field("digest1")
    private String digest1;

    private String status;

    @CreatedDate
    private Instant createDate;

    @LastModifiedDate
    private Instant updateDate;
}
