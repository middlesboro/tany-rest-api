package sk.tany.rest.api.domain.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String productId;
    private Integer quantity;
    private String title;
    private String image;
    private BigDecimal price;

    public CartItem(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
