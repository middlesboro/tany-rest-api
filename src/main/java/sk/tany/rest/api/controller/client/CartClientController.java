package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.*;
import sk.tany.rest.api.service.client.CartClientService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartClientController {

    private final CartClientService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartAddProductResponse> addProduct(@RequestBody CartItemRequest request) {
        String cartId = cartService.addProductToCart(request.getCartId(), request.getProductId(), request.getQuantity());
        CartAddProductResponse response = new CartAddProductResponse();
        response.setCartId(cartId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/carrier")
    public ResponseEntity<CartDto> addCarrier(@RequestBody CartCarrierRequest request) {
        return ResponseEntity.ok(cartService.addCarrier(request.getCartId(), request.getCarrierId()));
    }

    @PostMapping("/payment")
    public ResponseEntity<CartDto> addPayment(@RequestBody CartPaymentRequest request) {
        return ResponseEntity.ok(cartService.addPayment(request.getCartId(), request.getPaymentId()));
    }
}
