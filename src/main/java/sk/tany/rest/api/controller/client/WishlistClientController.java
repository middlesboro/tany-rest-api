package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.service.client.WishlistClientService;

import java.util.List;

@RestController
@RequestMapping("/client/wishlist")
@RequiredArgsConstructor
public class WishlistClientController {

    private final WishlistClientService wishlistClientService;

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addToWishlist(@PathVariable String productId) {
        wishlistClientService.addToWishlist(productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromWishlist(@PathVariable String productId) {
        wishlistClientService.removeFromWishlist(productId);
    }

    @GetMapping
    public List<String> getWishlist() {
        return wishlistClientService.getWishlistProductIds();
    }
}
