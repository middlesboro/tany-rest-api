package sk.tany.rest.api.domain.payment;

import lombok.Data;
import org.dizitart.no2.objects.Id;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class Payment {

    @Id
    private String id;
    private Long prestashopId;
    private String name;
    private PaymentType type;
    private BigDecimal price;
    private Integer order;
    private String description;
    private String image;
    private boolean active;
    private Instant createdDate;
    private Instant updateDate;
}
