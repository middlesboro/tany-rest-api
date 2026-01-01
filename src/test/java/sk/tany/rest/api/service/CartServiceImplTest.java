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

import sk.tany.rest.api.dto.CartItem;
import sk.tany.rest.api.dto.ProductDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductService productService;

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
        cartDto.setItems(new ArrayList<>());
        cartDto.getItems().add(new CartItem(productId, 1));

        Cart cartEntity = new Cart();
        cartEntity.setCartId(cartId);
        cartEntity.setItems(new ArrayList<>());
        cartEntity.getItems().add(new sk.tany.rest.api.domain.cart.CartItem(productId, 1));

        Cart savedEntity = new Cart();
        savedEntity.setCartId(cartId);
        savedEntity.setItems(new ArrayList<>());
        savedEntity.getItems().add(new sk.tany.rest.api.domain.cart.CartItem(productId, 2));

        CartDto savedDto = new CartDto();
        savedDto.setCartId(cartId);
        savedDto.setItems(new ArrayList<>());
        savedDto.getItems().add(new CartItem(productId, 2));

        ProductDto productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setTitle("Test Product");
        productDto.setPrice(BigDecimal.TEN);
        productDto.setImages(Collections.singletonList("img.jpg"));

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cartEntity));
        when(cartMapper.toDto(cartEntity)).thenReturn(cartDto);
        when(cartMapper.toEntity(cartDto)).thenReturn(cartEntity);
        when(cartRepository.save(any(Cart.class))).thenReturn(savedEntity);
        when(cartMapper.toDto(savedEntity)).thenReturn(savedDto);
        when(productService.findById(productId)).thenReturn(Optional.of(productDto));

        // Act
        String resultId = cartService.addProductToCart(cartId, productId, 1);

        // Assert
        assertThat(resultId).isEqualTo(cartId);
        // We verify that the logic inside addProductToCart correctly modifies the item
        assertThat(cartDto.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(cartDto.getItems().get(0).getTitle()).isEqualTo("Test Product");
        assertThat(cartDto.getItems().get(0).getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(cartDto.getItems().get(0).getImage()).isEqualTo("img.jpg");
    }

    @Test
    void addProductToCart_ShouldAddNewProduct_WhenProductDoesNotExist() {
        // Arrange
        String cartId = "cart1";
        String productId = "prod1";
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cartId);
        cartDto.setItems(new ArrayList<>());

        Cart cartEntity = new Cart();
        cartEntity.setCartId(cartId);
        cartEntity.setItems(new ArrayList<>());

        Cart savedEntity = new Cart();
        savedEntity.setCartId(cartId);
        savedEntity.setItems(new ArrayList<>());
        savedEntity.getItems().add(new sk.tany.rest.api.domain.cart.CartItem(productId, 1));

        CartDto savedDto = new CartDto();
        savedDto.setCartId(cartId);
        savedDto.setItems(new ArrayList<>());
        savedDto.getItems().add(new CartItem(productId, 1));

        ProductDto productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setTitle("New Product");
        productDto.setPrice(BigDecimal.ONE);
        productDto.setImages(Collections.singletonList("new.jpg"));

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cartEntity));
        when(cartMapper.toDto(cartEntity)).thenReturn(cartDto);
        when(cartMapper.toEntity(cartDto)).thenReturn(cartEntity);
        when(cartRepository.save(any(Cart.class))).thenReturn(savedEntity);
        when(cartMapper.toDto(savedEntity)).thenReturn(savedDto);
        when(productService.findById(productId)).thenReturn(Optional.of(productDto));

        // Act
        String resultId = cartService.addProductToCart(cartId, productId, 1);

        // Assert
        assertThat(resultId).isEqualTo(cartId);
        assertThat(cartDto.getItems()).hasSize(1);
        assertThat(cartDto.getItems().get(0).getProductId()).isEqualTo(productId);
        assertThat(cartDto.getItems().get(0).getQuantity()).isEqualTo(1);
        assertThat(cartDto.getItems().get(0).getTitle()).isEqualTo("New Product");
        assertThat(cartDto.getItems().get(0).getPrice()).isEqualTo(BigDecimal.ONE);
        assertThat(cartDto.getItems().get(0).getImage()).isEqualTo("new.jpg");
    }
}
