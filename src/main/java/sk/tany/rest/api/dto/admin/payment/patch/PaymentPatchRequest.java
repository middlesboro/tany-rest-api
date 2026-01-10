package sk.tany.rest.api.dto.admin.payment.patch;

import lombok.Data;
import sk.tany.rest.api.domain.payment.PaymentType;

import java.math.BigDecimal;

@Data
public class PaymentPatchRequest {
    private String name;
    private String description;
    private String image;
    private BigDecimal wholesalePrice;
    private BigDecimal priceWithoutVat;
    private BigDecimal price;
    private PaymentType type;
}
