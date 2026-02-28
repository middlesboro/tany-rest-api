package sk.tany.features.domain.payment;

import lombok.Data;
import org.springframework.data.annotation.Id;
import sk.tany.features.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class Payment extends BaseEntity {
    private Long prestashopId;
    private Integer iskladId;
    private String name;
    private PaymentType type;
    private BigDecimal price;
    private BigDecimal priceWithoutVat;
    private BigDecimal vatValue;
    private Integer order;
    private String description;
    private String image;
    private boolean active;
}
