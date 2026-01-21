package sk.tany.rest.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PriceBreakDown {
    private List<PriceItem> items = new ArrayList<>();
    private BigDecimal totalPrice;
    private BigDecimal totalPriceWithoutVat;
    private BigDecimal totalPriceVatValue;
}
