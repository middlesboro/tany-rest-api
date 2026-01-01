package sk.tany.rest.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.mapper.CartMapper;

import sk.tany.rest.api.dto.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductService productService;

    public CartDto getOrCreateCart(String cartId, String customerId) {
        CartDto cartDto = null;
        if (cartId != null) {
            cartDto = findById(cartId).orElse(null);
        }

        if (cartDto == null) {
            cartDto = new CartDto();
            cartDto.setItems(new ArrayList<>());
        }

        if (customerId != null) {
            cartDto.setCustomerId(customerId);
        }

        return save(cartDto);
    }

    public CartDto save(CartDto cartDto) {
        sk.tany.rest.api.domain.cart.Cart cart;
        if (cartDto.getCartId() != null) {
            cart = cartRepository.findById(cartDto.getCartId()).orElse(new sk.tany.rest.api.domain.cart.Cart());
        } else {
            cart = new sk.tany.rest.api.domain.cart.Cart();
        }
        cartMapper.updateEntityFromDto(cartDto, cart);
        return cartMapper.toDto(cartRepository.save(cart));
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
            cartDto.setItems(new ArrayList<>());
        }

        if (cartDto.getItems() == null) {
            cartDto.setItems(new ArrayList<>());
        }

        sk.tany.rest.api.dto.ProductDto productDto = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String image = (productDto.getImages() != null && !productDto.getImages().isEmpty())
                ? productDto.getImages().get(0)
                : null;

        int qty = (quantity == null || quantity <= 0) ? 1 : quantity;
        Optional<CartItem> existingItem = cartDto.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + qty);
            item.setTitle(productDto.getTitle());
            item.setPrice(productDto.getPrice());
            item.setImage(image);
        } else {
            CartItem newItem = new CartItem(productId, qty);
            newItem.setTitle(productDto.getTitle());
            newItem.setPrice(productDto.getPrice());
            newItem.setImage(image);
            cartDto.getItems().add(newItem);
        }

        return save(cartDto).getCartId();
    }
}
