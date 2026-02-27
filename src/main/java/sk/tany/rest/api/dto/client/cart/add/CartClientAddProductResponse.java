package sk.tany.rest.api.dto.client.cart.add;

import lombok.Data;
import sk.tany.rest.api.dto.PriceBreakDown;
import sk.tany.rest.api.dto.client.product.ProductDto;

import java.util.List;

@Data
public class CartClientAddProductResponse {
    private String cartId;
    private PriceBreakDown priceBreakDown;
    private ProductDto product;
    private List<ProductDto> suggestedProducts;
}
