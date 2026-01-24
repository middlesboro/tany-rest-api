package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.component.SecurityUtil;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.service.client.WishlistClientService;

@RestController
@RequestMapping("/client/wishlist")
@RequiredArgsConstructor
public class WishlistClientController {

    private final WishlistClientService wishlistClientService;
    private final SecurityUtil securityUtil;

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
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public Page<ProductClientDto> getWishlist(Pageable pageable) {
        String customerId = securityUtil.getLoggedInUserId();
        return wishlistClientService.getWishlist(customerId, pageable);
    }
}
