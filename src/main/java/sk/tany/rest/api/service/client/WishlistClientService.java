package sk.tany.rest.api.service.client;

import java.util.List;

public interface WishlistClientService {
    void addToWishlist(String productId);
    void removeFromWishlist(String productId);
    List<String> getWishlistProductIds();
}
