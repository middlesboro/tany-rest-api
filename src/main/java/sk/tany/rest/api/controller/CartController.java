package sk.tany.rest.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.CartAddProductResponse;
import sk.tany.rest.api.dto.CartItemRequest;
import sk.tany.rest.api.service.CartService;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartAddProductResponse> addProduct(@RequestBody CartItemRequest request) {
        String cartId = cartService.addProductToCart(request.getCartId(), request.getProductId(), request.getQuantity());
        CartAddProductResponse response = new CartAddProductResponse();
        response.setCartId(cartId);
        return ResponseEntity.ok(response);
    }
}
