package sk.tany.rest.api.dto;

import lombok.Data;
import sk.tany.rest.api.domain.payment.PaymentType;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentDto {
    private String id;
    private String name;
    private String description;
    private String image;
    private Integer order;
    private BigDecimal wholesalePrice;
    private BigDecimal priceWithoutVat;
    private BigDecimal price;
    private PaymentType type;
    private Instant createdDate;
    private Instant updateDate;
    private boolean selected;
}
