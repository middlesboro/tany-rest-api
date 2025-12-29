package sk.tany.rest.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.tany.rest.api.domain.cart.Cart;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.mapper.CartMapper;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    @Test
    void save() {
        CartDto cartDto = new CartDto();
        Cart cart = new Cart();
        when(cartMapper.toEntity(cartDto)).thenReturn(cart);
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toDto(cart)).thenReturn(cartDto);

        CartDto result = cartService.save(cartDto);

        assertEquals(cartDto, result);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void findAll() {
        when(cartRepository.findAll()).thenReturn(Collections.singletonList(new Cart()));
        when(cartMapper.toDto(any(Cart.class))).thenReturn(new CartDto());

        assertEquals(1, cartService.findAll().size());
    }

    @Test
    void findById() {
        Cart cart = new Cart();
        cart.setCartId("1");
        CartDto cartDto = new CartDto();
        cartDto.setCartId("1");
        when(cartRepository.findById("1")).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(cart)).thenReturn(cartDto);

        Optional<CartDto> result = cartService.findById("1");

        assertEquals("1", result.get().getCartId());
    }

    @Test
    void deleteById() {
        cartService.deleteById("1");
        verify(cartRepository, times(1)).deleteById("1");
    }
}
