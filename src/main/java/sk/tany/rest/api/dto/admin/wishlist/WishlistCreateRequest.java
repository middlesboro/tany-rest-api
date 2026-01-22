package sk.tany.rest.api.dto.admin.wishlist;

import lombok.Data;

@Data
public class WishlistCreateRequest {
    private String customerId;
    private String productId;
}
