package sk.tany.rest.api.dto.admin.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistAdminListResponse {
    private String customerId;
    private String customerName;
    private List<String> productNames;
}
