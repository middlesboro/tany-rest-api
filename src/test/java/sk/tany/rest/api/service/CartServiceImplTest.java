package sk.tany.rest.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.mapper.CartMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addProductToCart_ShouldUpdateQuantity_WhenProductExists() {
        // Arrange
        String cartId = "cart1";
        String productId = "prod1";
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setProducts(new HashMap<>());
        cartDto.getProducts().put(productId, 1);

        Cart cartEntity = new Cart();
        cartEntity.setCartId(cartId);
        cartEntity.setProducts(new HashMap<>());
        cartEntity.getProducts().put(productId, 1);

        Cart savedEntity = new Cart();
        savedEntity.setCartId(cartId);
        savedEntity.setProducts(new HashMap<>());
        savedEntity.getProducts().put(productId, 2);

        CartDto savedDto = new CartDto();
        savedDto.setCartId(cartId);
        savedDto.setProducts(new HashMap<>());
        savedDto.getProducts().put(productId, 2);

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cartEntity));
        when(cartMapper.toDto(cartEntity)).thenReturn(cartDto);
        when(cartMapper.toEntity(cartDto)).thenReturn(cartEntity);
        when(cartRepository.save(any(Cart.class))).thenReturn(savedEntity);
        when(cartMapper.toDto(savedEntity)).thenReturn(savedDto);

        // Act
        String resultId = cartService.addProductToCart(cartId, productId, 1);

        // Assert
        assertThat(resultId).isEqualTo(cartId);
        // We verify that the logic inside addProductToCart correctly modifies the map
        assertThat(cartDto.getProducts().get(productId)).isEqualTo(2);
    }

    @Test
    void addProductToCart_ShouldAddNewProduct_WhenProductDoesNotExist() {
        // Arrange
        String cartId = "cart1";
        String productId = "prod1";
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setProducts(new HashMap<>());

        Cart cartEntity = new Cart();
        cartEntity.setCartId(cartId);
        cartEntity.setProducts(new HashMap<>());

        Cart savedEntity = new Cart();
        savedEntity.setCartId(cartId);
        savedEntity.setProducts(new HashMap<>());
        savedEntity.getProducts().put(productId, 1);

        CartDto savedDto = new CartDto();
        savedDto.setCartId(cartId);
        savedDto.setProducts(new HashMap<>());
        savedDto.getProducts().put(productId, 1);


        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cartEntity));
        when(cartMapper.toDto(cartEntity)).thenReturn(cartDto);
        when(cartMapper.toEntity(cartDto)).thenReturn(cartEntity);
        when(cartRepository.save(any(Cart.class))).thenReturn(savedEntity);
        when(cartMapper.toDto(savedEntity)).thenReturn(savedDto);

        // Act
        String resultId = cartService.addProductToCart(cartId, productId, 1);

        // Assert
        assertThat(resultId).isEqualTo(cartId);
        assertThat(cartDto.getProducts().get(productId)).isEqualTo(1);
    }
}
