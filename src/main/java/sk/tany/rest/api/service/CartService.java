package sk.tany.rest.api.service;

import sk.tany.rest.api.dto.CartDto;
import java.util.List;
import java.util.Optional;

public interface CartService {
    CartDto getOrCreateCart(String cartId, String customerId);
    CartDto save(CartDto cartDto);
    List<CartDto> findAll();
    Optional<CartDto> findById(String id);
    void deleteById(String id);
    String addProductToCart(String cartId, String productId, Integer quantity);
}
