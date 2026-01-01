package sk.tany.rest.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.mapper.CartMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    public CartDto getOrCreateCart(String cartId, String customerId) {
        CartDto cartDto = null;
        if (cartId != null) {
            cartDto = findById(cartId).orElse(null);
        }

        if (cartDto == null) {
            cartDto = new CartDto();
            cartDto.setProducts(new HashMap<>());
        }

        if (customerId != null) {
            cartDto.setCustomerId(customerId);
        }

        return save(cartDto);
    }

    public CartDto save(CartDto cartDto) {
        return cartMapper.toDto(cartRepository.save(cartMapper.toEntity(cartDto)));
    }

    public List<CartDto> findAll() {
        return cartRepository.findAll().stream().map(cartMapper::toDto).collect(Collectors.toList());
    }

    public Optional<CartDto> findById(String id) {
        return cartRepository.findById(id).map(cartMapper::toDto);
    }

    public void deleteById(String id) {
        cartRepository.deleteById(id);
    }

    public String addProductToCart(String cartId, String productId, Integer quantity) {
        CartDto cartDto = null;
        if (cartId != null) {
            cartDto = findById(cartId).orElse(null);
        }

        if (cartDto == null) {
            cartDto = new CartDto();
            cartDto.setProducts(new HashMap<>());
        }

        if (cartDto.getProducts() == null) {
            cartDto.setProducts(new HashMap<>());
        }
        int qty = (quantity == null || quantity <= 0) ? 1 : quantity;
        cartDto.getProducts().merge(productId, qty, Integer::sum);

        return save(cartDto).getCartId();
    }
}
