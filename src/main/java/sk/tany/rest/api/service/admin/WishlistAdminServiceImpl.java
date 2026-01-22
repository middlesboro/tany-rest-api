package sk.tany.rest.api.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk.tany.rest.api.domain.wishlist.Wishlist;
import sk.tany.rest.api.domain.wishlist.WishlistRepository;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminDto;
import sk.tany.rest.api.dto.admin.wishlist.WishlistCreateRequest;
import sk.tany.rest.api.exception.ProductException;
import sk.tany.rest.api.mapper.WishlistMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistAdminServiceImpl implements WishlistAdminService {

    private final WishlistRepository wishlistRepository;
    private final WishlistMapper wishlistMapper;

    @Override
    public Page<WishlistAdminDto> findAll(Pageable pageable) {
        return wishlistRepository.findAll(pageable).map(wishlistMapper::toAdminDto);
    }

    @Override
    public WishlistAdminDto create(WishlistCreateRequest request) {
        // Check if already exists
        Optional<Wishlist> existing = wishlistRepository.findByCustomerIdAndProductId(request.getCustomerId(), request.getProductId());
        if (existing.isPresent()) {
             throw new ProductException.Conflict("Wishlist item already exists");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setCustomerId(request.getCustomerId());
        wishlist.setProductId(request.getProductId());
        wishlistRepository.save(wishlist);
        return wishlistMapper.toAdminDto(wishlist);
    }

    @Override
    public void delete(String id) {
        wishlistRepository.deleteById(id);
    }
}
