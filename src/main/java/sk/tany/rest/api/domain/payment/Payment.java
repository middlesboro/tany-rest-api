package sk.tany.rest.api.domain.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class Payment {

    private String id;
    private Long prestashopId;
    private String name;
    private PaymentType type;
    private BigDecimal price;
    private String description;
    private String image;
    private boolean active;
    private Instant createdDate;
    private Instant updateDate;
}
