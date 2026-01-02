package sk.tany.rest.api.service.admin;

import sk.tany.rest.api.dto.CartDto;
import java.util.List;
import java.util.Optional;

public interface CartAdminService {
    List<CartDto> findAll();
    Optional<CartDto> findById(String id);
    void deleteById(String id);
    CartDto save(CartDto cartDto);
}
