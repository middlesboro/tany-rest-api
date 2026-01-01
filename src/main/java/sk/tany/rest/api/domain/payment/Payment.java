package sk.tany.rest.api.domain.payment;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private PaymentType type;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedDate
    private Instant updateDate;
}
