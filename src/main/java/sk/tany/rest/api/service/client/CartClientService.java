package sk.tany.rest.api.service.client;

import sk.tany.rest.api.dto.CartDto;

public interface CartClientService {
    CartDto getOrCreateCart(String cartId, String customerId);
    CartDto save(CartDto cartDto);
    String addProductToCart(String cartId, String productId, Integer quantity);
}
