package sk.tany.rest.api.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.CartDto;
import sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateRequest;
import sk.tany.rest.api.dto.admin.cart.create.CartAdminCreateResponse;
import sk.tany.rest.api.dto.admin.cart.get.CartAdminGetResponse;
import sk.tany.rest.api.dto.admin.cart.list.CartAdminListResponse;
import sk.tany.rest.api.dto.admin.cart.patch.CartPatchRequest;
import sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateRequest;
import sk.tany.rest.api.dto.admin.cart.update.CartAdminUpdateResponse;
import sk.tany.rest.api.mapper.CartAdminApiMapper;
import sk.tany.rest.api.service.admin.CartAdminService;

import java.time.Instant;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/admin/carts")
@RequiredArgsConstructor
public class CartAdminController {

    private final CartAdminService cartService;
    private final CartAdminApiMapper cartAdminApiMapper;

    @PostMapping
    public ResponseEntity<CartAdminCreateResponse> createCart(@RequestBody CartAdminCreateRequest cartDto) {
        CartDto savedCart = cartService.save(cartAdminApiMapper.toDto(cartDto));
        return new ResponseEntity<>(cartAdminApiMapper.toCreateResponse(savedCart), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<CartAdminListResponse> getAllCarts(
            @RequestParam(required = false) String cartId,
            @RequestParam(required = false) Long orderIdentifier,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) Instant createDateFrom,
            @RequestParam(required = false) Instant createDateTo,
            Pageable pageable) {
        return cartService.findAll(cartId, orderIdentifier, customerName, createDateFrom, createDateTo, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartAdminGetResponse> getCartById(@PathVariable String id) {
        return cartService.findById(id)
                .map(cartAdminApiMapper::toGetResponse)
                .map(cart -> new ResponseEntity<>(cart, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PutMapping("/{id}")
    public ResponseEntity<CartAdminUpdateResponse> updateCart(@PathVariable String id, @RequestBody CartAdminUpdateRequest cartDto) {
        CartDto dto = cartAdminApiMapper.toDto(cartDto);
        dto.setCartId(id);
        CartDto updatedCart = cartService.save(dto);
        return new ResponseEntity<>(cartAdminApiMapper.toUpdateResponse(updatedCart), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CartAdminUpdateResponse> patchCart(@PathVariable String id, @RequestBody CartPatchRequest patchDto) {
        CartDto updatedCart = cartService.patch(id, patchDto);
        return new ResponseEntity<>(cartAdminApiMapper.toUpdateResponse(updatedCart), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable String id) {
        cartService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
