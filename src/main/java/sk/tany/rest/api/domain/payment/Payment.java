package sk.tany.rest.api.domain.payment;

import lombok.Data;
import org.dizitart.no2.repository.annotations.Id;
import sk.tany.rest.api.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class Payment implements BaseEntity {

    @Id
    private String id;
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
    private Instant createdDate;
    private Instant updateDate;

    @Override
    public void setLastModifiedDate(Instant date) {
        this.updateDate = date;
    }
    @Override
    public Instant getLastModifiedDate() {
        return this.updateDate;
    }
}
