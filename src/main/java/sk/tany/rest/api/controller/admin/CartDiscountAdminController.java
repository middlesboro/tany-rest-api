package sk.tany.rest.api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sk.tany.rest.api.dto.admin.cartdiscount.CartDiscountDto;
import sk.tany.rest.api.dto.admin.cartdiscount.create.CartDiscountCreateRequest;
import sk.tany.rest.api.dto.admin.cartdiscount.list.CartDiscountListResponse;
import sk.tany.rest.api.dto.admin.cartdiscount.update.CartDiscountUpdateRequest;
import sk.tany.rest.api.service.admin.CartDiscountAdminService;

@RestController
@RequestMapping("/api/admin/cart-discounts")
@RequiredArgsConstructor
@Tag(name = "Cart Discounts", description = "Admin Cart Discounts API")
public class CartDiscountAdminController {

    private final CartDiscountAdminService cartDiscountAdminService;

    @GetMapping
    @Operation(summary = "Get all cart discounts")
    public Page<CartDiscountListResponse> findAll(Pageable pageable) {
        return cartDiscountAdminService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cart discount by id")
    public CartDiscountDto findById(@PathVariable String id) {
        return cartDiscountAdminService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create cart discount")
    public CartDiscountDto create(@RequestBody CartDiscountCreateRequest request) {
        return cartDiscountAdminService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update cart discount")
    public CartDiscountDto update(@PathVariable String id, @RequestBody CartDiscountUpdateRequest request) {
        return cartDiscountAdminService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete cart discount")
    public void delete(@PathVariable String id) {
        cartDiscountAdminService.delete(id);
    }
}
