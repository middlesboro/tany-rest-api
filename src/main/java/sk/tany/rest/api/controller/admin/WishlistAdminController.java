package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.wishlist.WishlistAdminDto;
import sk.tany.rest.api.dto.admin.wishlist.WishlistCreateRequest;
import sk.tany.rest.api.service.admin.WishlistAdminService;

@RestController
@RequestMapping("/admin/wishlists")
@RequiredArgsConstructor
public class WishlistAdminController {

    private final WishlistAdminService wishlistAdminService;

    @GetMapping
    public Page<WishlistAdminDto> getAll(Pageable pageable) {
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
