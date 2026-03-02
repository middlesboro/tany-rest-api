package sk.tany.rest.api.dto.admin.wishlist;

import lombok.Data;
import java.time.Instant;

@Data
public class WishlistAdminDto {
    private String id;
    private String customerId;
    private String productId;
    private Instant createDate;
}
