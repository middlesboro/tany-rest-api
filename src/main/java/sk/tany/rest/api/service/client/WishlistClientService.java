package sk.tany.rest.api.service.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.client.product.ProductClientDto;

import java.util.List;

public interface WishlistClientService {
    void addToWishlist(String productId);
    void removeFromWishlist(String productId);
    List<String> getWishlistProductIds();
    Page<ProductClientDto> getWishlist(String customerId, Pageable pageable);
}
