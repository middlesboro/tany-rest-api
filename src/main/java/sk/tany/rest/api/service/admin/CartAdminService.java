package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.admin.cart.list.CartAdminListResponse;

import java.time.Instant;
import java.util.Optional;

public interface CartAdminService {
    Page<CartAdminListResponse> findAll(String cartId, Long orderIdentifier, String customerName, Instant createDateFrom, Instant createDateTo, Pageable pageable);
    Optional<CartDto> findById(String id);
    void deleteById(String id);
    CartDto save(CartDto cartDto);
    CartDto patch(String id, sk.tany.rest.api.dto.admin.cart.patch.CartPatchRequest patchDto);
}
