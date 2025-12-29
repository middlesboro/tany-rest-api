package sk.tany.rest.api.service;

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
public class CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

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
}
