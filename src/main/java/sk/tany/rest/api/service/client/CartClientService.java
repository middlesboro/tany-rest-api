package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.client.cart.add.CartClientAddProductResponse;

import java.util.Optional;

public interface CartClientService {
    Optional<CartDto> findCart(String cartId);
    CartDto getOrCreateCart(String cartId, String customerId);
    CartDto save(CartDto cartDto);
    CartClientAddProductResponse addProductToCart(String cartId, String productId, Integer quantity);
    String removeProductFromCart(String cartId, String productId);
    CartDto addCarrier(String cartId, String carrierId);
    CartDto addPayment(String cartId, String paymentId);
    CartDto addDiscount(String cartId, String code);
    CartDto removeDiscount(String cartId, String code);
}
