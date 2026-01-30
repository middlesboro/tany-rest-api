package sk.tany.rest.api.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminDto;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminListResponse;
import sk.tany.rest.api.dto.admin.wishlist.WishlistCreateRequest;

public interface WishlistAdminService {
    Page<WishlistAdminListResponse> findAll(Pageable pageable);
    WishlistAdminDto create(WishlistCreateRequest request);
    void delete(String id);
}
