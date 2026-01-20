package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.tany.rest.api.domain.cartdiscount.CartDiscount;
import sk.tany.rest.api.domain.cartdiscount.CartDiscountRepository;
import sk.tany.rest.api.dto.admin.cartdiscount.CartDiscountDto;
import sk.tany.rest.api.dto.admin.cartdiscount.create.CartDiscountCreateRequest;
import sk.tany.rest.api.dto.admin.cartdiscount.list.CartDiscountListResponse;
import sk.tany.rest.api.dto.admin.cartdiscount.update.CartDiscountUpdateRequest;
import sk.tany.rest.api.mapper.CartDiscountMapper;

@Service
@RequiredArgsConstructor
public class CartDiscountAdminService {

    private final CartDiscountRepository cartDiscountRepository;
    private final CartDiscountMapper cartDiscountMapper;

    public Page<CartDiscountListResponse> findAll(Pageable pageable) {
        return cartDiscountRepository.findAll(pageable)
                .map(cartDiscountMapper::toListResponse);
    }

    public CartDiscountDto findById(String id) {
        return cartDiscountRepository.findById(id)
                .map(cartDiscountMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Cart discount not found"));
    }

    @Transactional
    public CartDiscountDto create(CartDiscountCreateRequest request) {
        if (request.getCode() != null && cartDiscountRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Discount code already exists");
        }
        CartDiscount cartDiscount = cartDiscountMapper.toEntity(request);
        return cartDiscountMapper.toDto(cartDiscountRepository.save(cartDiscount));
    }

    @Transactional
    public CartDiscountDto update(String id, CartDiscountUpdateRequest request) {
        CartDiscount cartDiscount = cartDiscountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart discount not found"));

        if (request.getCode() != null && !request.getCode().equals(cartDiscount.getCode())) {
            if (cartDiscountRepository.existsByCode(request.getCode())) {
                throw new RuntimeException("Discount code already exists");
            }
        }

        cartDiscountMapper.updateEntityFromDto(request, cartDiscount);
        return cartDiscountMapper.toDto(cartDiscountRepository.save(cartDiscount));
    }

    @Transactional
    public void delete(String id) {
        cartDiscountRepository.deleteById(id);
    }
}
