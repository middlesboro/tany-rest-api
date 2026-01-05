package sk.tany.rest.api.domain.payment;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "besteron_payments")
public class BesteronPayment {

    @Id
    private String id;

    private String redirectUrl;
    private String transactionId;
    private String orderId;
    private String status;
    private String originalStatus;

    @CreatedDate
    @Indexed(expireAfterSeconds = 604800)
    private Instant createDate;

    @LastModifiedDate
    private Instant updateDate;

}
