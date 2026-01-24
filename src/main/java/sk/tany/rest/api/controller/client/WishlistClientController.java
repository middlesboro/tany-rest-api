package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.component.SecurityUtil;
import sk.tany.rest.api.dto.client.product.ProductClientDto;
import sk.tany.rest.api.service.client.WishlistClientService;

@RestController
@PreAuthorize("hasAnyRole('CUSTOMER')")
@RequestMapping("/api/wishlist")
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
    public Page<ProductClientDto> getWishlist(Pageable pageable) {
        String customerId = securityUtil.getLoggedInUserId();
        return wishlistClientService.getWishlist(customerId, pageable);
    }
}
