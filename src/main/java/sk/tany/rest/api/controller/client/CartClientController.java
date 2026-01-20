package sk.tany.rest.api.controller.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.client.cart.add.CartClientAddItemRequest;
import sk.tany.rest.api.dto.client.cart.add.CartClientAddProductResponse;
import sk.tany.rest.api.dto.client.cart.remove.CartClientRemoveItemRequest;
import sk.tany.rest.api.dto.client.cart.remove.CartClientRemoveItemResponse;
import sk.tany.rest.api.dto.client.cart.carrier.CartClientSetCarrierRequest;
import sk.tany.rest.api.dto.client.cart.carrier.CartClientSetCarrierResponse;
import sk.tany.rest.api.dto.client.cart.payment.CartClientSetPaymentRequest;
import sk.tany.rest.api.dto.client.cart.payment.CartClientSetPaymentResponse;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateRequest;
import sk.tany.rest.api.dto.client.cart.update.CartClientUpdateResponse;
import sk.tany.rest.api.mapper.CartClientApiMapper;
import sk.tany.rest.api.service.client.CartClientService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartClientController {

    private final CartClientService cartService;
    private final CartClientApiMapper cartClientApiMapper;

    @PutMapping
    public ResponseEntity<CartClientUpdateResponse> updateCart(@RequestBody CartClientUpdateRequest request) {
        CartDto dto = cartClientApiMapper.toDto(request);
        CartDto updatedCart = cartService.save(dto);
        return ResponseEntity.ok(cartClientApiMapper.toUpdateResponse(updatedCart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartClientAddProductResponse> addProduct(@RequestBody CartClientAddItemRequest request) {
        String cartId = cartService.addProductToCart(request.getCartId(), request.getProductId(), request.getQuantity());
        CartClientAddProductResponse response = new CartClientAddProductResponse();
        response.setCartId(cartId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items")
    public ResponseEntity<CartClientRemoveItemResponse> removeProduct(@RequestBody CartClientRemoveItemRequest request) {
        String cartId = cartService.removeProductFromCart(request.getCartId(), request.getProductId());
        CartClientRemoveItemResponse response = new CartClientRemoveItemResponse();
        response.setCartId(cartId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/carrier")
    public ResponseEntity<CartClientSetCarrierResponse> addCarrier(@RequestBody CartClientSetCarrierRequest request) {
        CartDto cartDto = cartService.addCarrier(request.getCartId(), request.getCarrierId());
        return ResponseEntity.ok(cartClientApiMapper.toSetCarrierResponse(cartDto));
    }

    @PostMapping("/payment")
    public ResponseEntity<CartClientSetPaymentResponse> addPayment(@RequestBody CartClientSetPaymentRequest request) {
        CartDto cartDto = cartService.addPayment(request.getCartId(), request.getPaymentId());
        return ResponseEntity.ok(cartClientApiMapper.toSetPaymentResponse(cartDto));
    }

    @PostMapping("/{cartId}/discount")
    public ResponseEntity<CartDto> addDiscount(@PathVariable String cartId, @RequestParam String code) {
        return ResponseEntity.ok(cartService.addDiscount(cartId, code));
    }

    @DeleteMapping("/{cartId}/discount")
    public ResponseEntity<CartDto> removeDiscount(@PathVariable String cartId, @RequestParam String code) {
        return ResponseEntity.ok(cartService.removeDiscount(cartId, code));
    }
}
