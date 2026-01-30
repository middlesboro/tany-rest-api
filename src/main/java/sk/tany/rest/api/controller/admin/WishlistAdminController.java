package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminDto;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminListResponse;
import sk.tany.rest.api.dto.admin.wishlist.WishlistCreateRequest;
import sk.tany.rest.api.service.admin.WishlistAdminService;

@RestController
@RequestMapping("/api/admin/wishlists")
@RequiredArgsConstructor
public class WishlistAdminController {

    private final WishlistAdminService wishlistAdminService;

    @GetMapping
    public Page<WishlistAdminListResponse> getAll(Pageable pageable) {
        return wishlistAdminService.findAll(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistAdminDto create(@RequestBody WishlistCreateRequest request) {
        return wishlistAdminService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        wishlistAdminService.delete(id);
    }
}
