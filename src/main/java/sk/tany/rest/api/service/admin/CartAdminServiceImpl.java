package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.cart.CartRepository;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.mapper.CartMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartAdminServiceImpl implements CartAdminService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    @Override
    public List<CartDto> findAll() {
        return cartRepository.findAll().stream().map(cartMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<CartDto> findById(String id) {
        return cartRepository.findById(id).map(cartMapper::toDto);
    }

    @Override
    public void deleteById(String id) {
        cartRepository.deleteById(id);
    }

    @Override
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
}
