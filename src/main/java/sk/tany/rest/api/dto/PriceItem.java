package sk.tany.rest.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PriceItem {
    private PriceItemType type;
    private BigDecimal priceWithVat;
    private BigDecimal priceWithoutVat;
    private BigDecimal vatValue;
    private String name;
    private String image;
    private Integer quantity;
    private String id;

    public PriceItem(PriceItemType type, String id, String name, Integer quantity, BigDecimal priceWithVat, BigDecimal priceWithoutVat, BigDecimal vatValue) {
        this(type, id, name, null, quantity, priceWithVat, priceWithoutVat, vatValue);
    }

    public PriceItem(PriceItemType type, String id, String name, String image, Integer quantity, BigDecimal priceWithVat, BigDecimal priceWithoutVat, BigDecimal vatValue) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.image = image;
        this.quantity = quantity;
        this.priceWithVat = priceWithVat;
        this.priceWithoutVat = priceWithoutVat;
        this.vatValue = vatValue;
    }
}
