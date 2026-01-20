package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.CartDto;

public interface CartClientService {
    CartDto getOrCreateCart(String cartId, String customerId);
    CartDto save(CartDto cartDto);
    String addProductToCart(String cartId, String productId, Integer quantity);
    String removeProductFromCart(String cartId, String productId);
    CartDto addCarrier(String cartId, String carrierId);
    CartDto addPayment(String cartId, String paymentId);
    CartDto addDiscount(String cartId, String code);
    CartDto removeDiscount(String cartId, String code);
}
